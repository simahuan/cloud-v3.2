智享追剧lib：pingsheng_lib
===============
一、依赖库(2个)
ViewPagerIndicatorLib
VitamioBundle

二、基本配置(参照pingshengDemo)
1、在主项目Application的onCreate()里面调用
MyDataCenter.getInstance().initData(this);

2、拷贝pingsheng_lib mainfest下面所有组件配置和权限配置

3、配置混淆见proguard-project.txt
拷贝混淆配置到主项目

4、在做打包混淆时，有找不到依赖jar情况，请检查一下proguard-project.txt文件：
看看-libraryjars 对应的路径是否正确。

5、如果发现support_v4 签名冲突时，请保证主项目的suport_v4和所有依赖库里面的是同一个签名版本。
简单做法：用pingsheng_lib里面android-support-v4.jar覆盖掉主项目和其他lib项目的android-support-v4.jar 


三、功能入口
1、追剧Fragment
com.wefi.zhuiju.activity.follow.FollowFragment

2、Wifi
com.wefi.zhuiju.activity.mine.wifi.WifiConfigActivityNew

3、互联网
com.wefi.zhuiju.activity.mine.internet.InternetStateActivity

4、存储
com.wefi.zhuiju.activity.mine.StorageActivityNew

5、访客
com.wefi.zhuiju.activity.mine.share2.Share2Activity

6、神器升级
com.wefi.zhuiju.activity.mine.upgrade.FirmwareUpgradeActivity

7、关机
com.wefi.zhuiju.activity.mine.ShutdownActivity

8、常见问题：如果有需要，请将asset目录拷贝到主项目
com.wefi.zhuiju.activity.mine.problems.ProblemsActivity

9、实验室
com.wefi.zhuiju.activity.mine.lab.LabActivity

四、联系方式：
QQ:372763226  张世威