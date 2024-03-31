#
# base recipe: meta/recipes-core/zlib/zlib_1.2.11.bb
# base branch: master
# base commit: d886fa118c930d0e551f2a0ed02b35d08617f746
#

SUMMARY = "compression library"
DESCRIPTION = "zlib is a library implementing the deflate compression method found \
 in gzip and PKZIP.  This package includes the development support \
 files."
HOMEPAGE = "http://zlib.net/"

inherit debian-package
require recipes-debian/sources/zlib.inc
DEBIAN_UNPACK_DIR = "${WORKDIR}/${BPN}-${REPACK_PV}"

LICENSE = "Zlib"
LIC_FILES_CHKSUM = " \
file://zlib.h;beginline=4;endline=23;md5=627e6ecababe008a45c70e318ae7014e \
"

CVE_PRODUCT = "zlib:zlib"

SRC_URI += "file://ldflags-tests.patch \
            file://run-ptest \
"

FILESEXTRAPATHS =. "${COREBASE}/meta/recipes-core/zlib/zlib:"

CFLAGS += "-D_REENTRANT"

RDEPENDS_${PN}-ptest += "make"

inherit ptest

do_configure() {
	LDCONFIG=true ./configure --prefix=${prefix} --shared --libdir=${libdir} --uname=GNU
}

do_compile() {
	oe_runmake shared
}

do_install() {
	oe_runmake DESTDIR=${D} install
}

do_install_ptest() {
	install ${B}/examplesh ${D}${PTEST_PATH}
}

# move run-time libraries to ${libdir}
# "debian/rules install" also does the same things
do_install_append_class-target() {
	if [ ${base_libdir} != ${libdir} ]
	then
		mkdir -p ${D}/${base_libdir}
		mv ${D}/${libdir}/libz.so.* ${D}/${base_libdir}
		 libname=`readlink ${D}/${libdir}/libz.so`
		 ln -sf ${@oe.path.relative("${libdir}", "${base_libdir}")}/$libname ${D}/${libdir}/libz.so
	fi
}

BBCLASSEXTEND = "native nativesdk"
