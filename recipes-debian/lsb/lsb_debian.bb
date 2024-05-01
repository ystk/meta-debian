SUMMARY = "Linux Standard Base init script functionality"
DESCRIPTION = "The Linux Standard Base (http://www.linuxbase.org/) is a standard \
core system that third-party applications written for Linux can \
depend upon."

LICENSE = "GPLv2 & BSD-3-Clause"
LIC_FILES_CHKSUM = " \
	file://lsb_release;beginline=3;endline=18;md5=c6eddaa75f90d557582cda8d81bd26a1 \
	file://init-functions;beginline=3;endline=28;md5=253d9c2ede4edede28a861d7e77e33c5 \
"

inherit debian-package
require recipes-debian/sources/lsb.inc
DEBIAN_UNPACK_DIR = "${WORKDIR}/work"

inherit python-dir ptest

SRC_URI += " \
    file://run-ptest \
    file://0001-test-Fix-range-of-exit-on-error-to-make-tests-keep-g.patch \
"

do_install(){
	# Install files for lsb-base
	install -d ${D}${nonarch_base_libdir}/${BPN}/init-functions.d
	install -m 0644 ${S}/init-functions ${D}${nonarch_base_libdir}/${BPN}
	install -m 0644 ${S}/init-functions.d/20-left-info-blocks ${D}${nonarch_base_libdir}/${BPN}/init-functions.d

	# Many init scripts of Poky require /etc/init.d/functions
	install -d ${D}${sysconfdir}/init.d
	ln -sf ${nonarch_base_libdir}/${BPN}/init-functions ${D}${sysconfdir}/init.d/functions

	# Install files for lsb-release
	install -d ${D}${bindir}
	install -m 0755 ${S}/lsb_release ${D}${bindir}
	install -d ${D}${datadir}/pyshared
	install lsb_release.py ${D}${datadir}/pyshared
	install -d ${D}${PYTHON_SITEPACKAGES_DIR} \
	           ${D}${libdir}/python3/site-packages
	ln -s ../../../share/pyshared/lsb_release.py ${D}${PYTHON_SITEPACKAGES_DIR}/
	ln -s ../../../share/pyshared/lsb_release.py ${D}${libdir}/python3/site-packages/

	if [ "${TARGET_ARCH}" = "x86_64" ]; then
	       # don't symlink if usrmerge is in DISTRO_FEATURES as it manages the symlink
		if ${@bb.utils.contains('DISTRO_FEATURES','usrmerge','false','true',d)} && \
		  [ "${base_libdir}" != "${base_prefix}/lib64" ]; then
			lnr ${D}${base_libdir} ${D}${base_prefix}/lib64
		fi
		cd ${D}${base_libdir}
		ln -sf ld-linux-x86-64.so.2 ld-lsb-x86-64.so.2
		ln -sf ld-linux-x86-64.so.2 ld-lsb-x86-64.so.3
	fi
	if [ "${TARGET_ARCH}" = "i586" ] || [ "${TARGET_ARCH}" = "i686" ]; then
		cd ${D}${base_libdir}
		ln -sf ld-linux.so.2 ld-lsb.so.2
		ln -sf ld-linux.so.2 ld-lsb.so.3
	fi

	if [ "${TARGET_ARCH}" = "powerpc64" ]; then
		if [ "${base_libdir}" != "${base_prefix}/lib64" ]; then
			lnr ${D}${base_libdir} ${D}${base_prefix}/lib64
		fi
		cd ${D}${base_libdir}
		ln -sf ld64.so.1 ld-lsb-ppc64.so.2
		ln -sf ld64.so.1 ld-lsb-ppc64.so.3
	fi
	if [ "${TARGET_ARCH}" = "powerpc" ]; then
		cd ${D}${base_libdir}
		ln -sf ld.so.1 ld-lsb-ppc32.so.2
		ln -sf ld.so.1 ld-lsb-ppc32.so.3
	fi
}

do_install_ptest() {
    install -m 755 ${S}/test/lsb-test.sh ${D}${PTEST_PATH}
}

PACKAGES =+ "${PN}-release"

FILES_${PN} += " \
    ${@'${base_prefix}/lib64' if d.getVar('TARGET_ARCH') == ('x86_64' or 'powerpc64') and '${base_libdir}' != '${base_prefix}/lib64' else ''} \
    ${base_libdir} \
    ${nonarch_base_libdir}/lsb \
"
FILES_${PN}-release = " \
    ${bindir}/lsb_release \
    ${datadir}/pyshared/* \
    ${PYTHON_SITEPACKAGES_DIR}/lsb_release.py \
    ${libdir}/python3/site-packages/lsb_release.py \
"

# Required for running lsb_release
RDEPENDS_${PN}-release += " \
    python-subprocess \
    python3-core \
"

RPROVIDES_${PN} += "lsb-base initd-functions"
PKG_${PN} = "lsb-base"
