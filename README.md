# DistributedCloudsim

一个在分布式主机群中运行的基于cloudsim plus的模拟器

## 设计框架

整体框架如下图所示:

![image](https://user-images.githubusercontent.com/65942634/233537026-5c259428-d48e-45f5-8419-896c4e9b62ee.png)

每个服务器单独运行一个模拟器来模拟一个数据中心，每个服务器有自己的数据中心配置文件和客户需求文件，然后这些文件都是每个服务器上都有一份的，同时还共同拥有一份网络配置文件，每个服务器上的数据中心的决策器都可以以及自己获得的讯息来进行分布式的决策。服务器之间的信息沟通和同步有两种方式，一种是借助socket连接的网络通信，这里的作用主要是用来同步各个服务器模拟的时间，还有一种是借助数据库，每个服务器共同连接到同一个数据库，将自己数据中心的状态还有要转移出去的任务放到数据库中去。

整体创建的基础数据类型的模块如下:

![image](https://user-images.githubusercontent.com/65942634/233537066-15d9427b-31e6-4d74-86de-d6ab10e2d9c3.png)

程序运行的流程图如下所示：

![功能概述 (6)](https://user-images.githubusercontent.com/65942634/233537275-43e98837-52f0-4d79-9c17-1ba3e37468e3.jpg)

## 运行结果

这里用2个服务器来模拟两个数据中心。服务器的配置为2核4G，每个核的配置为：Intel(R) Xeon(R) Gold 6161 CPU @ 2.20GHz。然后每个数据中心有10000台主机，每个数据中心拥有1000个客户，每个客户都需要创建10台虚拟机，有10个任务在上面执行。使用的是循环适配的方法来决策任务应该流向哪个数据中心。模拟30s所花费的时间是2132.987s，即35.55分钟。最后得到的时间占比分析如下：

![image](https://user-images.githubusercontent.com/65942634/233537371-f700c3f5-8273-4aa0-9384-401544c0671f.png)

![image](https://user-images.githubusercontent.com/65942634/233537376-30126078-bd7b-4021-812a-af4f465238d3.png)

可以看出主要的时间都花费在了更新主机的状态信息上，总共花费了1504秒，占比70%。这主要是因为设置的模拟间隔是0.1秒，所以每0.1s就需要把1万个主机的状态信息都同步到数据库中去，数据提交所需要的时间多。再然后多的时间就是和其他数据中心同步用的时间花费了327秒，占比15%，因为每模拟一段0.1秒就需要同步其他数据中心，需要等待其将模拟的时间发送过来。再然后时间占比第三的才是cloudsim plus 模拟运行的时间，花费了294秒，这也就说明了真实用在模拟数据中心的行为的时间反而是比较少的。其他的一些时间也就更少了，基本可以忽略不计。

**randomOffload（随机分配）**
这里依据是使用2个数据中心来进行模拟，其中一个数据中心c1有2个客户需求，一个在一开始0s的时候提交上去，一个在10s的时候提交上去；另一个数据中心c2有5个客户需求，一个在一开始0s的时候提交上去，一个在10s的时候提交上去，剩余三个在15s的时候全部提交上去。各个数据中心的主机都管够。最后运行的结果如下：

c1数据中心：

![image](https://user-images.githubusercontent.com/65942634/233537472-99b0afca-78b2-4e9e-ae1a-d8a9fd78359c.png)


c2数据中心：

![image](https://user-images.githubusercontent.com/65942634/233537491-8e2ec8a2-8aa3-40ee-8004-e44078e8f3cb.png)

可以看到c1数据中心的2个客户需求都被随机卸载到了c2数据中心，开始c2运行的时间分别是0s和10s。而c2数据中心的5个客户需求中也有3个被随机卸载到了c1，在c1开始运行的时间为0s、11s和16s。如此结果说明随机卸载策略可以正常运行。

*其他具体的结果可见报告文档*
