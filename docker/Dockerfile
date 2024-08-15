FROM debian:buster

ARG UID
ARG BUILDDIR

USER root

ENV USER deby
ENV PASS deby
ENV HOME /home/deby
ENV POKY_GIT_URL https://git.yoctoproject.org/git/poky
ENV POKY_REV warrior

RUN bash -c 'if test -n "$http_proxy"; then echo "Acquire::http::proxy \"$http_proxy\";" > /etc/apt/apt.conf.d/99proxy; fi'
RUN apt-get update && \
    apt-get install -y --no-install-recommends sudo iptables \
	gawk wget git-core diffstat unzip texinfo gcc-multilib \
	build-essential chrpath socat cpio python python3 python3-pip \
	python3-pexpect xz-utils debianutils iputils-ping file locales \
	openssh-client python3-debian

RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && locale-gen
RUN useradd -d $HOME -U -m -s /bin/bash -u $UID $USER
RUN usermod -a -G sudo $USER
RUN echo "$USER:$PASS" | chpasswd
RUN echo "%$USER  ALL=(ALL:ALL) NOPASSWD: ALL" > /etc/sudoers.d/debygrp
RUN chmod 0440 /etc/sudoers.d/debygrp

# Setup directory's owner before switching to user.
RUN mkdir $HOME/downloads
RUN chown $USER:$USER $HOME/downloads

ADD gitproxy /usr/bin
RUN chmod +x /usr/bin/gitproxy
RUN bash -c 'if test -n "$http_proxy"; then sed -i -e "s#\(http_proxy=\"\).*#\1$http_proxy\"#" /usr/bin/gitproxy; fi'

USER $USER

ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

RUN bash -c 'if test -n "$http_proxy"; then git config --global http.proxy "$http_proxy"; \
                                            git config --global core.gitproxy gitproxy; fi'
RUN bash -c 'if test -n "$https_proxy"; then git config --global https.proxy "$https_proxy"; fi'
RUN bash -c 'if test -n "$no_proxy"; then git config --global core.noproxy "$no_proxy"; fi'

RUN git clone --depth 1 -b $POKY_REV $POKY_GIT_URL $HOME/poky
RUN mkdir -p $HOME/poky/meta-debian
RUN mkdir -p $HOME/$BUILDDIR
RUN ln -sf $HOME/downloads $HOME/$BUILDDIR/downloads

WORKDIR $HOME
