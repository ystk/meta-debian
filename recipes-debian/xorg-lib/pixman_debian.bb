SUMMARY = "Pixman: Pixel Manipulation library"

DESCRIPTION = "Pixman provides a library for manipulating pixel regions \
-- a set of Y-X banded rectangles, image compositing using the \
Porter/Duff model and implicit mask generation for geometric primitives \
including trapezoids, triangles, and rectangles."

require ${COREBASE}/meta/recipes-graphics/xorg-lib/xorg-lib-common.inc

inherit debian-package
require recipes-debian/sources/pixman.inc
DEBIAN_PATCH_TYPE = "quilt"
FILESPATH_append = ":${COREBASE}/meta/recipes-graphics/xorg-lib/pixman"

inherit ptest

# see http://cairographics.org/releases/ - only even minor versions are stable
UPSTREAM_CHECK_REGEX = "pixman-(?P<pver>\d+\.(\d*[02468])+(\.\d+)+)"

LICENSE = "MIT & MIT-style & PD"
LIC_FILES_CHKSUM = "file://COPYING;md5=14096c769ae0cbb5fcb94ec468be11b3 \
                    file://pixman/pixman-matrix.c;endline=25;md5=ba6e8769bfaaee2c41698755af04c4be \
                    file://pixman/pixman-arm-neon-asm.h;endline=24;md5=9a9cc1e51abbf1da58f4d9528ec9d49b \
                   "
DEPENDS += "zlib libpng"
BBCLASSEXTEND = "native nativesdk"

IWMMXT = "--disable-arm-iwmmxt"
LOONGSON_MMI = "--disable-loongson-mmi"
# If target supports neon then disable the 'simd' (ie VFPv2) fallback, otherwise disable neon.
NEON = "${@bb.utils.contains("TUNE_FEATURES", "neon", "--disable-arm-simd", "--disable-arm-neon" ,d)}"

EXTRA_OECONF = "--disable-gtk ${IWMMXT} ${LOONGSON_MMI} ${NEON}"
EXTRA_OECONF_class-native = "--disable-gtk"
EXTRA_OECONF_class-nativesdk = "--disable-gtk"

SRC_URI += "\
            file://0001-ARM-qemu-related-workarounds-in-cpu-features-detecti.patch \
	    file://0001-test-utils-Check-for-FE_INVALID-definition-before-us.patch \
            file://run-ptest \
"

do_compile_ptest() {
    oe_runmake check TESTS=
}

do_install_ptest() {
    cp -r ${S}/pixman ${D}${PTEST_PATH}
    cp -r ${S}/test ${D}${PTEST_PATH}
    cp -r ${S}/demos ${D}${PTEST_PATH}
    cp -r ${B}/pixman/* ${D}${PTEST_PATH}/pixman/
    cp -r ${B}/test/* ${D}${PTEST_PATH}/test/

    # Overwrite wrapper scripts
    cp ${B}/pixman/.libs/* ${D}${PTEST_PATH}/pixman/
    cp ${B}/test/.libs/* ${D}${PTEST_PATH}/test/

    install -m 755 ${S}/test-driver ${D}${PTEST_PATH}
    install -m 644 ${B}/Makefile ${D}${PTEST_PATH}
    install -m 644 ${B}/test/*.la ${D}${PTEST_PATH}/test/

    sed -i \
        -e 's|^VPATH =.*$|VPATH = .|g' \
        -e 's|^Makefile:.*$|Makefile:|g' \
        -e 's|^all-am:.*$|all-am:|g' \
        -e 's|^srcdir =.*$|srcdir = .|g' \
        -e 's|^top_srcdir =.*$|top_srcdir = .|g' \
        ${D}${PTEST_PATH}/Makefile

    for d in pixman test demos; do
        install -m 644 ${B}/$d/Makefile ${D}${PTEST_PATH}/$d/
        sed -i \
            -e 's|^VPATH =.*$|VPATH = .|g' \
            -e 's|^Makefile:.*$|Makefile:|g' \
            -e 's|^all-am:.*$|all-am:|g' \
            -e 's|^srcdir =.*$|srcdir = .|g' \
            -e 's|^top_srcdir =.*$|top_srcdir = ..|g' \
            ${D}${PTEST_PATH}/$d/Makefile
    done
}

RDEPENDS_${PN}-ptest += "make gawk"

REQUIRED_DISTRO_FEATURES = ""

# CVE-2023-37769: This is for test program (stress-test) and it's not distributed by binary package.
CVE_CHECK_WHITELIST = "CVE-2023-37769"
