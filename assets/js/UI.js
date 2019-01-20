/**
 * Created by pisen on 2015/5/28.
 */
var UI = {
    /*=================/
     tab选项卡
     tabNavBox:'#tabboxs', 				最大的BOX容器
     tabNavObj:'.tabNav',  				选项卡UL样式
     tabNavBtn:'li',								选项卡下面的LI
     tabContentObj:'.tabContent', 	控制下面box
     tabContent:'.list',						控制box下面的隐藏显示层
     currentClass:'current', 				选项卡的样式
     eventType:'click',    				选项卡的点击方式
     onActiveTab: null							选项卡的点击的扩展方法
     controlUnit:true,    					控制选项可不可会
     controlClass:null							启用选项卡样式
     * 2014-06-13 陈建 创建
     ====================*/
    Collapsebox: function (options) { }
};

UI.Collapsebox = function(options){
    var defaults ={
        parent:"#accordion",
        panelBox:".panel",
        openbox:".panel-collapse",
        clickstyle:".switch",
        arrowstyle:'.iconfont',
        openclass:"glyphicon-chevron-up",
        closeclass:"glyphicon-chevron-down",
        Radio:true,
        clickEve:null
    };
    var opt = $.extend(defaults,options);
    //为触发元素添加单击事件，在回调函数里打开折叠元素，此时由于上面已经指定了parent属性，所以Bootstrap会为我们自动将其他折叠组件关闭
    var clickObj=$(opt.clickstyle,opt.parent);
    var panelcomObj=$(opt.openbox,opt.parent);//		var panelcomObj=$(self).parents(opt.parent).find(opt.openbox);
    clickObj.on('click', function () {
        var self = this;
        var ind=clickObj.index($(self));
        var panelcom=(ind==-1?$(self).parent().find(opt.openbox):panelcomObj.eq(ind));
        if(opt.Radio){
            $(opt.parent).find("."+opt.openclass).removeClass(opt.openclass).addClass(opt.closeclass);
            panelcomObj.each(function(){
                if($(this).hasClass('in')){
                    $(this).slideUp(200,function(){
                        $(this).removeClass('in');
                    });
                }
            });
            //debugger;
            if(panelcom.hasClass("in")){
                panelcom.stop().slideUp(200,function(){
                    $(this).removeClass('in');
                });
                $(self).find(opt.arrowstyle).removeClass(opt.openclass).addClass(opt.closeclass);
            }else{
                panelcom.stop().slideDown(200,function(){
                    $(this).addClass('in');
                });
                $(self).find(opt.arrowstyle).removeClass(opt.closeclass).addClass(opt.openclass);
            }
            //回调函数
            if(typeof(opt.clickEve)=="function"){
                opt.clickEve(panelcom,ind);
            }
        }else{
            if(panelcom.hasClass("in")){
                panelcom.stop().slideUp(200,function(){
                    $(this).css({
                        height: "auto"
                    });
                    $(this).removeClass('in');
                });
                $(self).find(opt.arrowstyle).removeClass(opt.openclass).addClass(opt.closeclass);

            }else{
                panelcom.stop().slideDown(200,function(){
                    $(this).css({
                        height: "auto"
                    });
                    $(this).addClass('in');
                });
                $(self).find(opt.arrowstyle).removeClass(opt.closeclass).addClass(opt.openclass);
            }
            //回调函数
            if(typeof(opt.clickEve)=="function"){
                opt.clickEve(panelcom,ind);
            }
        }
    });
    $(opt.parent).on('show.bs.collapse', function () {
        //
    });
};