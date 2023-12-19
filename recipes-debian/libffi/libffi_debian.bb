SUMMARY = "A portable foreign function interface library"
HOMEPAGE = "http://sourceware.org/libffi/"
DESCRIPTION = "The `libffi' library provides a portable, high level programming interface to various calling \
conventions.  This allows a programmer to call any function specified by a call interface description at run \
time. FFI stands for Foreign Function Interface.  A foreign function interface is the popular name for the \
interface that allows code written in one language to call code written in another language.  The `libffi' \
library really only provides the lowest, machine dependent layer of a fully featured foreign function interface.  \
A layer must exist above `libffi' that handles type conversions for values passed between the two languages."

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3610bb17683a0089ed64055416b2ae1b"

inherit debian-package
require recipes-debian/sources/libffi.inc
FILESPATH_append = ":${COREBASE}/meta/recipes-support/libffi/libffi"

SRC_URI += "\
           file://not-win32.patch \
	   file://0001-mips-Use-compiler-internal-define-for-linux.patch \
           file://0001-mips-fix-MIPS-softfloat-build-issue.patch \
           file://0001-libffi-Support-musl-x32-build.patch \
           file://0001-testsuite-Prevent-deletion-of-test-binaries.patch \
           file://run-ptest \
           "

EXTRA_OECONF += "--disable-builddir"
EXTRA_OEMAKE_class-target = "LIBTOOLFLAGS='--tag=CC'"
inherit autotools texinfo ptest

FILES_${PN}-dev += "${libdir}/libffi-${PV}"

do_compile_ptest() {
    # Prevent execution of test binaries on host environment
    sed -i \
        -e 's/dg-do run/dg-do link/g' \
        ${S}/testsuite/libffi.call/*

    # Compile test binaries
    oe_runmake -k -C testsuite check RUNTESTFLAGS="-a"

    # Restore the previous change to make sure tests will be executed
    # on target environment
    sed -i \
        -e 's/dg-do link/dg-do run/g' \
        ${S}/testsuite/libffi.call/*
}

do_install_ptest() {
    cp -r ${S}/testsuite/ ${D}${PTEST_PATH}/
    cp -r ${B}/* ${D}${PTEST_PATH}/
    cp -r ${B}/.libs/* ${D}${PTEST_PATH}/

    # Replace CC and CXX with true command to prevent compilation on target
    # environment (instead, do nothing)
    sed -i \
        -e 's|^set CC_FOR_TARGET .*|set CC_FOR_TARGET "true"|g' \
        -e 's|^set CXX_FOR_TARGET .*|set CXX_FOR_TARGET "true"|g' \
        ${D}${PTEST_PATH}/testsuite/site.exp

    sed -i \
        -e 's|^set srcdir .*|set srcdir "."|g' \
        -e 's|^set objdir .*|set objdir "."|g' \
        ${D}${PTEST_PATH}/testsuite/site.exp

    sed -i \
        -e 's|^VPATH =.*$|VPATH = .|g' \
        -e 's|^Makefile:.*$|Makefile:|g' \
        -e 's|^srcdir =.*|srcdir = .|g' \
        -e 's|^top_srcdir =.*|top_srcdir = .|g' \
        -e 's|^abs_srcdir =.*|abs_srcdir = .|g' \
        -e 's|^abs_top_srcdir =.*|abs_top_srcdir = .|g' \
        -e "s|${BPN}-${PV}/config.guess|./config.guess|g" \
        ${D}${PTEST_PATH}/Makefile

    for d in include testsuite; do
        sed -i \
            -e 's|^VPATH =.*$|VPATH = .|g' \
            -e 's|^Makefile:.*$|Makefile:|g' \
            -e 's|^srcdir =.*|srcdir = .|g' \
            -e 's|^top_srcdir =.*|top_srcdir = ..|g' \
            -e 's|^abs_srcdir =.*|abs_srcdir = .|g' \
            -e 's|^abs_top_srcdir =.*|abs_top_srcdir = ..|g' \
            ${D}${PTEST_PATH}/$d/Makefile
    done

    # Remove rpath from test binaries
    find ${D}${PTEST_PATH}/testsuite/ -name '*.exe' \
        -exec patchelf --remove-rpath {} \;
}

DEPENDS += "${@bb.utils.contains('PTEST_ENABLED', '1', 'dejagnu-native patchelf-native', '', d)}"
RDEPENDS_${PN}-ptest += "bash dejagnu make"

# Doesn't compile in MIPS16e mode due to use of hand-written
# assembly
MIPS_INSTRUCTION_SET = "mips"

BBCLASSEXTEND = "native nativesdk"

