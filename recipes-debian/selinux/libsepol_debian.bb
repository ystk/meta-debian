# base recipe: meta-selinux/recipes-security/selinux/libsepol_2.8.bb
# base branch: warrior

SUMMARY = "SELinux binary policy manipulation library"
DESCRIPTION = "libsepol provides an API for the manipulation of SELinux binary policies. \
It is used by checkpolicy (the policy compiler) and similar tools, as well \
as by programs like load_policy that need to perform specific transformations \
on binary policies such as customizing policy boolean settings."
HOMEPAGE = "https://github.com/SELinuxProject"
SECTION = "base"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=a6f89e2100d9b6cdffcea4f398e37343"

inherit debian-package
require recipes-debian/sources/libsepol.inc

SRC_URI += "file://0001-src-Makefile-fix-includedir-in-libsepol.pc.patch"

inherit lib_package

# Change RANLIB for cross compiling, use host-tools $(AR) rather than
# local ranlib.
EXTRA_OEMAKE += "RANLIB='$(AR) s'"

DEPENDS += "flex-native"

do_compile() {
	oe_runmake all \
		INCLUDEDIR='${STAGING_INCDIR}' \
		LIBDIR='${STAGING_LIBDIR}'
}

do_install() {
	oe_runmake install \
		DESTDIR="${D}" \
		PREFIX="${prefix}" \
		INCLUDEDIR="${includedir}" \
		LIBDIR="${libdir}" \
		SHLIBDIR="${base_libdir}" \
		SYSTEMDDIR="${systemd_unitdir}"
}

BBCLASSEXTEND = "native"
