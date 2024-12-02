#
# linux-src.bbclass
#
# This class file is shared by linux-base_git.bb and
# linux-libc-headers-base_git.bb. Please define the following
# controllable variables in a global configuration file.
#
# LINUX_GIT_URI: 'dirname' of the repository URI (must begin with git://)
# LINUX_GIT_PROTOCOL: protocol (git, http, https, etc.)
# LINUX_GIT_PREFIX: prefix for LINUX_GIT_REPO
# LINUX_GIT_REPO: 'basename' of the repository URI
# LINUX_GIT_BRANCH: a branch name
# LINUX_GIT_SRCREV: a commit hash (or a branch name)
#
# Example:
#   LINUX_GIT_URI = "git://git.kernel.org/pub/scm/linux/kernel/git/stable"
#   LINUX_GIT_PROTOCOL = "https"
#   LINUX_GIT_PREFIX = ""
#   LINUX_GIT_REPO = "linux-stable.git"
#   LINUX_GIT_BRANCH = "linux-3.10.y"
#   LINUX_GIT_SRCREV = "e7a59c7f266809d17dcde20fd2055e23e7eb6895"
#

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"

LINUX_GIT_URI ??= "git://git.kernel.org/pub/scm/linux/kernel/git/cip"
LINUX_GIT_PROTOCOL ??= "https"
LINUX_GIT_PREFIX ??= ""
LINUX_GIT_REPO ??= "linux-cip.git"
LINUX_GIT_BRANCH ??= "linux-4.19.y-cip"
LINUX_GIT_SRCREV ??= "${AUTOREV}"
LINUX_VERSION ??= "4.19"

SRC_URI = "${LINUX_GIT_URI}/${LINUX_GIT_PREFIX}${LINUX_GIT_REPO};branch=${LINUX_GIT_BRANCH};protocol=${LINUX_GIT_PROTOCOL}"

SRCREV = "${LINUX_GIT_SRCREV}"
PV = "${LINUX_VERSION}+git${SRCPV}"

S = "${WORKDIR}/git"

# use GITPKGVTAG for cve-check
inherit gitpkgv
do_cve_check[depends] += "linux-base:do_fetch"
CVE_VERSION ??= "${@d.getVar('GITPKGVTAG').split('-')[0]}"
