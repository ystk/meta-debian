SUMMARY = "PNG image format decoding library"
HOMEPAGE = "http://www.libpng.org/"
SECTION = "libs"
LICENSE = "Libpng"
LIC_FILES_CHKSUM = "file://LICENSE;md5=12b4ec50384c800bc568f519671b120c \
                    file://png.h;endline=144;md5=15ae15f53376306868259924a9db4e05 \
"
DEPENDS = "zlib"

inherit debian-package
require recipes-debian/sources/libpng1.6.inc
BPN = "libpng"

UPSTREAM_CHECK_URI = "http://libpng.org/pub/png/libpng.html"

BINCONFIG = "${bindir}/libpng-config ${bindir}/libpng16-config"

inherit autotools binconfig-disabled pkgconfig ptest

#DPN = "libpng1.6"
# override 'S' set by debian-package-ng
#S = "${WORKDIR}/${BPN}-${DEB_SRC_VERSION}"
#SRC_URI[dsc.md5sum] = "840c5a4648c85655c3f4d89c038581fa"
#SRC_URI[dsc.sha256sum] = "b4d875fa27ce7a682ec0a5b078d71d1353b745e8b12a79af21e7478538ffbb87"

# Work around missing symbols
EXTRA_OECONF_append_class-target = " ${@bb.utils.contains("TUNE_FEATURES", "neon", "--enable-arm-neon=on", "--enable-arm-neon=off" ,d)}"

PACKAGES =+ "${PN}-tools"

FILES_${PN}-tools = "${bindir}/png-fix-itxt ${bindir}/pngfix ${bindir}/pngcp"

SRC_URI += " \
    file://run-ptest \
"

do_compile_ptest() {
    oe_runmake check TESTS=""
}

do_install_ptest() {
    install -m 755 ${S}/test-driver ${D}${PTEST_PATH}/
    install -m 644 ${S}/pngtest.png ${D}${PTEST_PATH}/
    cp -r ${B}/* ${D}${PTEST_PATH}/
    cp -r ${S}/tests/ ${D}${PTEST_PATH}/
    cp -r ${S}/contrib/pngsuite/ ${D}${PTEST_PATH}/contrib/
    cp -r ${S}/contrib/testpngs/ ${D}${PTEST_PATH}/contrib/
    for d in . mips powerpc; do
        cp ${B}/$d/.libs/* ${D}${PTEST_PATH}/
    done

    # Remove these files to avoid bash dependency
    rm ${D}${PTEST_PATH}/*-libtool
    rm ${D}${PTEST_PATH}/config.status

    install -m 644 ${S}/*.h ${D}${PTEST_PATH}/
    install -m 644 ${S}/*.dfa ${D}${PTEST_PATH}/
    install -m 644 ${S}/scripts/*.dfa ${D}${PTEST_PATH}/scripts/
    for d in . contrib/libtests contrib/tools mips powerpc; do
        install -m 644 ${S}/$d/*.c ${D}${PTEST_PATH}/$d/
    done

    install -m 644 ${S}/scripts/options.awk ${D}${PTEST_PATH}/scripts/
    sed -i \
        -e 's|^#!/bin/awk|#!/usr/bin/awk|g' \
        ${D}${PTEST_PATH}/scripts/options.awk

    install -m 644 ${B}/Makefile ${D}${PTEST_PATH}/
    sed -i \
        -e 's|^VPATH =.*$|VPATH = .|g' \
        -e 's|^Makefile:.*$|Makefile:|g' \
        -e 's|^srcdir =.*|srcdir = .|g' \
        -e 's|^top_srcdir =.*|top_srcdir = .|g' \
        -e 's|^abs_srcdir =.*|abs_srcdir = .|g' \
        -e 's|^abs_top_srcdir =.*|abs_top_srcdir = .|g' \
        ${D}${PTEST_PATH}/Makefile
}

RDEPENDS_${PN}-ptest += "make gawk"

BBCLASSEXTEND = "native nativesdk"
