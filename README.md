# MinIO 的安装与使用

## MinIO 简介

  [MinIO](https://min.io/)是一个基于Apache License v2.0开源协议的分布式对象存储服务。兼容亚马逊S3云存储接口，使用 MinIO 为机器学习、分析和应用程序数据工作负载构建高性能基础架构。

## MinIO中的基础概念

- S3 ，是**Simple Storage Service**简单对存储的简称，S3提供了一个简单的服务接口，可随时的在web上的任何位置存储和检索任何数量的数据；
- Obejct ，MinIO存储的基本单位，任何存在MinIO中的事物都可以称之为一个对象（Object），如文档、图片等；
-  Bucket，MinIO中一组对象的逻辑上的隔离单位，不同Bucket中的Object是相互隔离的；
- Drive，MinIO存储的物理磁盘位置，MinIO中的数据都存在Drive中；
- Set，一组Drive的集合，在分布式的环境中，自动规划一个或者多个Set，每个Set中的Drive分布在不同的位置，容灾处理。

## MinIO安装

### MinIO Server 安装

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

### 访问web admin

从启动的日志输出中得到默认的用户名/密码（minioadmin/minioadmin）和web访问地址 http://127.0.0.1:9000, 使用用户和密码进行登录，第一次登录后建议修改默认的minioadmin密码，左侧菜单 **Identity -> ServiceAccount**

![image-20221105154041778](./img/image-20221105154041778.png)

### MinIO Client 安装

```shell
# 下载
wget https://dl.min.io/client/mc/release/linux-amd64/mc
# 添加执行权限
chmod +x mc
mv mc /usr/local/bin/mc
# 添加minio
mc alias set local http://localhost:9000 minioadmin minioadmin
# 添加成功输出信息
mc: Configuration written to `/root/.mc/config.json`. Please update your access credentials.
mc: Successfully created `/root/.mc/share`.
mc: Initialized share uploads `/root/.mc/share/uploads.json` file.
mc: Initialized share downloads `/root/.mc/share/downloads.json` file.
```

### MinIO Client 操作

  MinIO Client (mc)为ls，cat，cp，mirror，diff，find等UNIX命令提供了一种替代方案。它支持文件系统和兼容Amazon S3的云存储服务（AWS Signature v2和v4）。

### mc命令

```shell
ls       列出文件和文件夹。
mb       创建一个存储桶或一个文件夹。
cat      显示文件和对象内容。
pipe     将一个STDIN重定向到一个对象或者文件或者STDOUT。
share    生成用于共享的URL。
cp       拷贝文件和对象。
mirror   给存储桶和文件夹做镜像。
find     基于参数查找文件。
diff     对两个文件夹或者存储桶比较差异。
rm       删除文件和对象。
events   管理对象通知。
watch    监听文件和对象的事件。
policy   管理访问策略。
session  为cp命令管理保存的会话。
config   管理mc配置文件。
update   检查软件更新。
version  输出版本信息。
```

### mb 创建Bucket

```shell
# 在local上创建一个test的Bucket
mc mb local/test

Bucket created successfully `local/test`.
```

### cp 拷贝一个对象到MinIO Server中

```shell
echo 'hello minio'> hellominio.txt
mc cp hellominio.txt local/test

/root/hellominio.txt:                                 12 B / 12 B
```

### ls 查看Bucket和Obeject

```shell
mc ls local
# 刚刚创建的 test Bucket 中存在一个 hellominio.txt
[2022-11-05 20:32:30 CST]    12B STANDARD hellominio.txt
```

通过MinIO server web 查看

![image-20221105203507772](./img/image-20221105203507772.png)

### cat 显示对象的内容

```shell
mc cat local/test/hellominio.txt

hello minio
```

### pipe 通过pipe将stdin输出到对象

```shell
echo 'pipe test'> pipe.txt
# 将输出重定向到local/test/pipe.txt
cat pipe.txt | mc pipe local/test/pipe.txt
# 查看生成的对象内容
mc cat local/test/pipe.txt

pipe test
```

### find 查找Object

```shell
# 查找所有 txt 文本文件
mc find local/test --name "*.txt"
local/test/hellominio.txt
local/test/pipe.txt
```

### share 将对象以带有认证和过期机制的URL机制共享

#### share download 共享下载

`share download`命令生成不需要access key和secret key即可下载的URL，过期参数设置成最大有效期（不大于7天），过期之后权限自动回收。

```shell
# 为hellominio.txt生成有限期3天的URL
 mc share download --expire 72h local/test/hellominio.txt
URL: http://localhost:9000/test/hellominio.txt
Expire: 3 days 0 hours 0 minutes 0 seconds
Share: http://localhost:9000/test/hellominio.txt?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20221105%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20221105T131412Z&X-Amz-Expires=259200&X-Amz-SignedHeaders=host&X-Amz-Signature=fd3df574faf2276ff47995a361ee1260093bee273d546c76d9ca9f955cc04cdd

```

### share upload 共享上传

`share upload`命令生成不需要access key和secret key即可上传的URL。过期参数设置成最大有效期（不大于7天），过期之后权限自动回收。 Content-type参数限制只允许上传指定类型的文件。

```shell
# 生成上传helloworld
mc share upload local/test/helloword.txt

RL: http://localhost:9000/test/helloword.txt
Expire: 7 days 0 hours 0 minutes 0 seconds
Share: curl http://localhost:9000/test/ -F x-amz-credential=minioadmin/20221105/us-east-1/s3/aws4_request -F x-amz-date=20221105T132630Z -F x-amz-signature=cfd000de2c2494c7fed1c232e5b0a41bb8fb4c7c96a4cb595506910bb7a6a23d -F bucket=test -F policy=eyJleHBpcmF0aW9uIjoiMjAyMi0xMS0xMlQxMzoyNjozMC41MjRaIiwiY29uZGl0aW9ucyI6W1siZXEiLCIkYnVja2V0IiwidGVzdCJdLFsiZXEiLCIka2V5IiwiaGVsbG93b3JkLnR4dCJdLFsiZXEiLCIkeC1hbXotZGF0ZSIsIjIwMjIxMTA1VDEzMjYzMFoiXSxbImVxIiwiJHgtYW16LWFsZ29yaXRobSIsIkFXUzQtSE1BQy1TSEEyNTYiXSxbImVxIiwiJHgtYW16LWNyZWRlbnRpYWwiLCJtaW5pb2FkbWluLzIwMjIxMTA1L3VzLWVhc3QtMS9zMy9hd3M0X3JlcXVlc3QiXV19 -F x-amz-algorithm=AWS4-HMAC-SHA256 -F key=helloword.txt -F file=@<FILE>

# 通过生成的URL上传
echo 'hello world' >helloworld.txt
#  -F file=./helloworld.txt
curl http://localhost:9000/test/ -F x-amz-credential=minioadmin/20221105/us-east-1/s3/aws4_request -F x-amz-date=20221105T132630Z -F x-amz-signature=cfd000de2c2494c7fed1c232e5b0a41bb8fb4c7c96a4cb595506910bb7a6a23d -F bucket=test -F policy=eyJleHBpcmF0aW9uIjoiMjAyMi0xMS0xMlQxMzoyNjozMC41MjRaIiwiY29uZGl0aW9ucyI6W1siZXEiLCIkYnVja2V0IiwidGVzdCJdLFsiZXEiLCIka2V5IiwiaGVsbG93b3JkLnR4dCJdLFsiZXEiLCIkeC1hbXotZGF0ZSIsIjIwMjIxMTA1VDEzMjYzMFoiXSxbImVxIiwiJHgtYW16LWFsZ29yaXRobSIsIkFXUzQtSE1BQy1TSEEyNTYiXSxbImVxIiwiJHgtYW16LWNyZWRlbnRpYWwiLCJtaW5pb2FkbWluLzIwMjIxMTA1L3VzLWVhc3QtMS9zMy9hd3M0X3JlcXVlc3QiXV19 -F x-amz-algorithm=AWS4-HMAC-SHA256 -F key=helloword.txt -F file=./helloworld.txt
# 查看是否上传成功
mc ls local/test
[2022-11-05 20:52:32 CST]    12B STANDARD hellominio.txt
[2022-11-05 21:27:49 CST]    16B STANDARD helloword.txt
[2022-11-05 20:41:34 CST]    10B STANDARD pipe.txt
```

### share list 查看当前的share列表

```shell
# share download list
mc share list download
# share upload list
mc share list upload
```

### rm 删除Bucket和Object

```shell
# 删除一个对象
mc rm local/test/hellominio.txt
Removed `local/test/hellominio.txt`

# 删除一个Bucket及其中的所有对象
mc rm --recursive --force local/test
Removed `local/test/hellominio.txt`.
Removed `local/test/helloword.txt`.
Removed `local/test/pipe.txt`.

# 按时间删除
# 删除一天前的对象
mc rm --force --older-than=1d local/test/

```

> 更多命令使用参考官方使用文档，[MinIO Client指南](http://docs.minio.org.cn/docs/master/minio-client-complete-guide)

## SpringBoot集成MinIO



