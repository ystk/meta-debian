# base recipe: meta-openembedded/meta-networking/recipes-filter/libnftnl/libnftnl_1.1.1.bb
# base branch: warrior

SUMMARY = "Library for low-level interaction with nftables Netlink's API over libmnl"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=79808397c3355f163c012616125c9e26"
SECTION = "libs"
DEPENDS = "libmnl"

inherit debian-package
require recipes-debian/sources/libnftnl.inc

DEBIAN_QUILT_PATCHES = ""

SRC_URI += "file://0001-Move-exports-before-symbol-definition.patch \
            file://0002-avoid-naming-local-function-as-one-of-printf-family.patch \
            file://0003-configure.ac-Add-serial-tests.patch \
            file://run-ptest \
           "

inherit autotools pkgconfig ptest

DEPENDS = "libmnl"
RDEPENDS_${PN}-ptest += "bash python3-core make"

TESTDIR = "tests"

do_compile_ptest() {
    cp -rf ${S}/build-aux .
    oe_runmake buildtest-TESTS
}

do_install_ptest() {
    cp -rf ${B}/build-aux ${D}${PTEST_PATH}
    install -d ${D}${PTEST_PATH}/${TESTDIR}
    cp -rf ${B}/${TESTDIR}/Makefile ${D}${PTEST_PATH}/${TESTDIR}

    # the binaries compiled in ${TESTDIR} will look for a compiler to
    # use, which will cause failures. Substitute the binaries in
    # ${TESTDIR}/.libs instead
    cp -rf ${B}/${TESTDIR}/.libs/* ${D}${PTEST_PATH}/${TESTDIR}

    # Alter the Makefile so that it does not try and rebuild anything in
    # other nonexistent paths before running the actual tests
    sed -i 's/^Makefile/_Makefile/'  ${D}${PTEST_PATH}/${TESTDIR}/Makefile
}

