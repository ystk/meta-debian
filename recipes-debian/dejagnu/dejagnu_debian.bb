# base recipe: meta/recipes-devtools/dejagnu/dejagnu_1.6.2.bb
# base branch: dunfell
# base commit: dbe19706ec01c1eaa1d377a792e7d825054050b0
SUMMARY = "GNU unit testing framework, written in Expect and Tcl"
DESCRIPTION = "DejaGnu is a framework for testing other programs. Its purpose \
is to provide a single front end for all tests."
HOMEPAGE = "https://www.gnu.org/software/dejagnu/"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"
SECTION = "devel"

inherit debian-package
require recipes-debian/sources/dejagnu.inc

DEBIAN_QUILT_PATCHES = ""

DEPENDS += "expect-native"
RDEPENDS_${PN} = "expect"

inherit autotools

BBCLASSEXTEND = "native"
