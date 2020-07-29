{
    "variables": {
        "aws_access_key": "{{env `AWS_ACCESS_KEY`}}",
        "aws_secret_key": "{{env `AWS_SECRET_KEY`}}",
        "aws_region": "{{env `AWS_REGION`}}"
    },
    "builders": [
      {
        "type": "amazon-ebs",
        "access_key": "{{user `aws_access_key`}}",
        "secret_key": "{{user `aws_secret_key`}}",
        "region": "{{user `aws_region`}}",
        "instance_type": "t2.micro",
        "source_ami_filter": {
          "filters": {
            "virtualization-type": "hvm",
            "name": "CentOS Linux 7 x86_64 HVM EBS ENA 1901_01-b7ee8a69-ee97-4a49-9e68-afaee216db2e-*",
            "root-device-type": "ebs"
          },
          "owners": [
            "679593333241"
          ],
          "most_recent": true
        },
        "ssh_username": "centos",
        "ami_name": "jenkins-{{timestamp}}"
      }
    ],
    "provisioners": [
      {
        "type": "shell",
        "script": "install-jenkins-script.sh"
      }
    ]
  }