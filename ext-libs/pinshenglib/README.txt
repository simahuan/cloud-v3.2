智享追剧lib：PinShengLib
===============

一、代码git地址
https://bitbucket.org/
name：ForPinSheng
pwd: forpinsheng123
mail: zhangshiwei@werouter.com

//只读库
ViewPagerIndicatorLib
VitamioBundle
PinshengDemo
PinShengLib

二、依赖库(2个)
ViewPagerIndicatorLib
VitamioBundle

二、基本配置(参照PinshengDemo)
1、在主项目Application的onCreate()里面调用
MyDataCenter.getInstance().initData(this);

2、拷贝PinShengLib mainfest下面所有组件配置和权限配置

3、配置混淆见proguard-project.txt
拷贝混淆配置到主项目

4、在做打包混淆时，有找不到依赖jar情况，请检查一下proguard-project.txt文件：
看看-libraryjars 对应的路径是否正确。

5、如果发现support_v4 签名冲突时，请保证主项目的suport_v4和所有依赖库里面的是同一个签名版本。
简单做法：用pingsheng_lib里面android-support-v4.jar覆盖掉主项目和其他lib项目的android-support-v4.jar 


三、功能入口
1、追剧Fragment
com.wefi.zhuiju.activity.follow.FollowFragment

2、直接看在线视频
 SnUtil.goOnlineVideo(getActivity());


四、联系方式：
智享科技 010-57482383
