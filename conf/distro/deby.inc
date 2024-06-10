DISTRO_NAME = "Deby"
DISTRO_VERSION = "10.0"
DISTRO_CODENAME = "buster"
SDK_VENDOR = "-debysdk"
SDK_VERSION := "${DISTRO_VERSION}"

MAINTAINER = "meta-debian Development Team"
TARGET_VENDOR = "-deby"

# Using the package versions provided in the security update mirror
# is now necessary to pull updates by Debian LTS
DEBIAN_SECURITY_UPDATE_MIRROR ?= "http://security.debian.org/debian-security/pool/updates"

# Extended LTS Updates for Debian 10 Buster
DEBIAN_ELTS_SECURITY_UPDATE_MIRROR ?= "http://deb.freexian.com/extended-lts/pool"

# Add an eventhandler that generates DEBIAN_SRC_URI information
# from Debian apt repository.
INHERIT += "debian-source"

# currently only Debian 10 buster is tested
# In Debian, tested distro names given by distro_identifier()
# depend on the following command and files:
# 1. lsb_release -ir => Debian-testing
# 2. /etc/lsb-release => (no package provide this file)
# 3. /etc/os-release => debian
SANITY_TESTED_DISTROS ?= " \
Debian-testing \n \
debian \n \
"

# use toolchain mode for Debian instead of the default
TCMODE ?= "deby"

# preferred providers and versions
require conf/distro/include/debian-preferred-provider.inc

# WORKAROUND for https://bugzilla.yoctoproject.org/show_bug.cgi?id=13338
TOOLCHAIN_TARGET_TASK_remove = "target-sdk-provides-dummy"

ROOT_HOME = "/root"
