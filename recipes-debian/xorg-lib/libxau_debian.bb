SUMMARY = "Xau: X Authority Database library"

DESCRIPTION = "libxau provides the main interfaces to the X11 \
authorisation handling, which controls authorisation for X connections, \
both client-side and server-side."

require ${COREBASE}/meta/recipes-graphics/xorg-lib/xorg-lib-common.inc

# clear SRC_URI
SRC_URI = ""
inherit debian-package
require recipes-debian/sources/libxau.inc
DEBIAN_PATCH_TYPE = "nopatch"
DEBIAN_UNPACK_DIR = "${WORKDIR}/${XORG_PN}-${PV}"

inherit gettext ptest

SRC_URI += " \
    file://run-ptest \
    file://0001-Autest.c-Fix-return-code.patch \
"

LICENSE = "MIT-style"
LIC_FILES_CHKSUM = "file://COPYING;md5=7908e342491198401321cec1956807ec"

DEPENDS += " xorgproto"
PROVIDES = "xau"

XORG_PN = "libXau"

do_compile_ptest() {
    oe_runmake check TESTS=
}

do_install_ptest() {
    install -m 644 ${S}/test-driver ${D}${PTEST_PATH}
    install -m 644 ${S}/*.c ${D}${PTEST_PATH}

    install -m 644 ${B}/Makefile ${D}${PTEST_PATH}
    sed -i \
        -e 's|^VPATH =.*$|VPATH = .|g' \
        -e 's|^Makefile:.*$|Makefile:|g' \
        -e 's|^srcdir =.*|srcdir = .|g' \
        -e 's|^top_srcdir =.*|top_srcdir = .|g' \
        -e 's|^abs_srcdir =.*|abs_srcdir = .|g' \
        -e 's|^abs_top_srcdir =.*|abs_top_srcdir = .|g' \
        ${D}${PTEST_PATH}/Makefile

    install -m 644 ${B}/*.lo ${D}${PTEST_PATH}
    install -m 644 ${B}/*.la ${D}${PTEST_PATH}
    install -m 644 ${B}/*.o ${D}${PTEST_PATH}
    install -m 755 ${B}/.libs/* ${D}${PTEST_PATH}
}

RDEPENDS_${PN}-ptest += "make gawk"

BBCLASSEXTEND = "native nativesdk"
