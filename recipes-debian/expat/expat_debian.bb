#
# base recipe: meta/recipes-core/expat/expat_2.2.5.bb
# base branch: master
# base commit: d886fa118c930d0e551f2a0ed02b35d08617f746
#
SUMMARY = "A stream-oriented XML parser library"
DESCRIPTION = "Expat is an XML parser library written in C. It is a stream-oriented \
parser in which an application registers handlers for things the parser might find \
in the XML document (like start tags)"

inherit debian-package
require recipes-debian/sources/expat.inc
DEBIAN_UNPACK_DIR = "${WORKDIR}/libexpat-R_${@d.getVar('PV',True).replace('.','_')}"
S = "${DEBIAN_UNPACK_DIR}/expat"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://COPYING;md5=5b8620d98e49772d95fc1d291c26aa79"

#  Don't build doc to reduce dependency, it depends on docbook-to-man
SRC_URI += "file://disable-build-doc.patch \
            file://autotools.patch \
            file://run-ptest \
           "

FILESEXTRAPATHS =. "${FILE_DIRNAME}/files:${COREBASE}/meta/recipes-core/expat/expat:"

RDEPENDS_${PN}-ptest += "bash"

inherit autotools lib_package ptest

do_configure_prepend () {
	rm -f ${S}/conftools/libtool.m4
}

do_compile_ptest() {
	oe_runmake -C ${B}/tests runtests runtestspp
}

do_install_ptest_class-target() {
	install -m 755 ${B}/tests/.libs/* ${D}${PTEST_PATH}
}

BBCLASSEXTEND = "native nativesdk"
