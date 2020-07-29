#!/bin/bash
sudo yum install epel-release -y
sudo yum install wget java -y 
sudo wget -O /etc/yum.repos.d/jenkins.repo http://pkg.jenkins.io/redhat/jenkins.repo
sudo rpm --import http://pkg.jenkins.io/redhat/jenkins.io.key
sudo yum install jenkins -y 
sudo systemctl start jenkins
sudo systemctl enable jenkins