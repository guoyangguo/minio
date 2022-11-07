# minio 的安装与使用

## 安装

### minio server 安装

```shell
# 下载 minio server
wget https://dl.min.io/server/minio/release/linux-amd64/minio
# 添加可执行权限
chmod +x minio
sudo mv minio /usr/local/bin/
# 创建数据文件夹
mkdir ~/minio
# 启动minio server
minio server ~/minio --console-address :9090
# 启动输出结果
Formatting 1st pool, 1 set(s), 1 drives per set.
WARNING: Host local has more than 0 drives of set. A host failure will result in data becoming unavailable.
WARNING: Detected default credentials 'minioadmin:minioadmin', we recommend that you change these values with 'MINIO_ROOT_USER' and 'MINIO_ROOT_PASSWORD' environment variables
MinIO Object Storage Server
Copyright: 2015-2022 MinIO, Inc.
License: GNU AGPLv3 <https://www.gnu.org/licenses/agpl-3.0.html>
Version: RELEASE.2022-10-29T06-21-33Z (go1.19.2 linux/amd64)

Status:         1 Online, 0 Offline.
API: http://172.16.159.228:9000  http://172.17.0.1:9000  http://172.18.0.1:9000  http://127.0.0.1:9000
RootUser: minioadmin
RootPass: minioadmin
Console: http://172.16.159.228:9090 http://172.17.0.1:9090 http://172.18.0.1:9090 http://127.0.0.1:9090
RootUser: minioadmin
RootPass: minioadmin

Command-line: https://min.io/docs/minio/linux/reference/minio-mc.html#quickstart
   $ mc alias set myminio http://172.16.159.228:9000 minioadmin minioadmin

Documentation: https://min.io/docs/minio/linux/index.html
```

### 访问minio server web admin

![image-20221105154041778](./img/image-20221105154041778.png)

从启动的日志输出中得到默认的用户名/密码（minioadmin/minioadmin）和web访问地址 http://127.0.0.1:9000

### minio client 安装

```shell
wget https://dl.min.io/client/mc/release/linux-amd64/mc
chmod +x mc
mc alias set myminio/ http://MINIO-SERVER MYUSER MYPASSWORD
```

