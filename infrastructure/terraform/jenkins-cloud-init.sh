#!/bin/bash

set -e

export DEBIAN_FRONTEND=noninteractive

echo "=== DevFlow Jenkins VM bootstrap ==="

# --------------------------------------------------------
# Base packages
# --------------------------------------------------------

apt-get update

apt-get install -y \
  ca-certificates \
  curl \
  gnupg \
  git \
  unzip

# --------------------------------------------------------
# Docker repository
# --------------------------------------------------------

install -m 0755 -d /etc/apt/keyrings

curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  -o /etc/apt/keyrings/docker.asc

chmod a+r /etc/apt/keyrings/docker.asc

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
  > /etc/apt/sources.list.d/docker.list

# --------------------------------------------------------
# Docker installation
# --------------------------------------------------------

apt-get update

apt-get install -y \
  docker-ce \
  docker-ce-cli \
  containerd.io \
  docker-buildx-plugin \
  docker-compose-plugin

# --------------------------------------------------------
# Allow Jenkins VM administrator to use Docker
# --------------------------------------------------------

usermod -aG docker devflow

# --------------------------------------------------------
# Jenkins home
# --------------------------------------------------------

mkdir -p /opt/jenkins

# --------------------------------------------------------
# Jenkins image
# --------------------------------------------------------

docker pull jenkins/jenkins:lts-jdk21

# --------------------------------------------------------
# Jenkins container
# --------------------------------------------------------

docker run -d \
  --name jenkins \
  --restart unless-stopped \
  -p 127.0.0.1:8080:8080 \
  -p 50000:50000 \
  -v /opt/jenkins:/var/jenkins_home \
  jenkins/jenkins:lts-jdk21

echo "=== Jenkins VM bootstrap completed ==="

