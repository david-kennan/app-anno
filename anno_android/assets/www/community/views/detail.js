define([
    "dojo/_base/array",
    "dojo/dom",
    "dojo/dom-class",
    "dojo/dom-geometry",
    "dojo/dom-style",
    "dojo/query",
    "dojo/_base/lang",
    "dojo/_base/connect",
    "dojo/window",
    "dojo/has",
    "dojo/sniff",
    "dijit/registry",
    "dojox/css3/transit",
    "dojox/gesture/swipe",
    "dojox/mobile/TransitionEvent",
    "dojo/store/Memory",
    "dojox/mvc/getStateful"
],
    function (arrayUtil, dom, domClass, domGeom, domStyle, query, lang, connect, win, has, sniff, registry, transit, swipe, TransitionEvent, Memory, getStateful)
    {
        var _connectResults = [];
        var eventsModel = null;
        var currentIndex = 0;
        var textDataAreaShown = false;
        var swipeInst;
        var app = null;
        var screenshotScrollTop = null, annoCommentsScrollTop = null;

        var adjustSize = function()
        {
            var viewPoint = win.getBox();
            var parentBox = domGeom.getMarginBox("headingDetail");
            domStyle.set("imgDetailScreenshot", "width", (viewPoint.w-30)+"px");

            var h = (viewPoint.h-parentBox.h-6);
            domStyle.set("textDataAreaContainer", "width", (viewPoint.w-30-6-10)+"px");
            domStyle.set("annoTextDetail", "width", (viewPoint.w-30-6-10-8)+"px");
            domStyle.set("textDataAreaContainer", "height", (h-11)+"px");
            domStyle.set("annoCommentsContainer", "height", (h-104)+"px");

            domStyle.set("appNameTextBox", "width", (viewPoint.w-30-6-10-40)+"px");

            domStyle.set("screenshotTooltipDetail", "width", (viewPoint.w-30-viewPoint.w*0.10)+"px");


            //var tooltipWidget = registry.byId('textTooltip');
            //domStyle.set(tooltipWidget.domNode, 'top', (parseInt(domStyle.get(tooltipWidget.domNode, 'top'))+14)+'px');
            //domStyle.set("screenshotContainerDetail", "height", (viewPoint.h-parentBox.h)+"px");
        };

        var screenshotImageOnload = function()
        {
            window.setTimeout(function(){
                var viewPoint = win.getBox();
                var tooltipWidget = registry.byId('textTooltip');

                var orignialRatio = dom.byId('imgDetailScreenshot').naturalHeight/dom.byId('imgDetailScreenshot').naturalWidth;
                dom.byId("imgDetailScreenshot").width = (viewPoint.w-30);
                dom.byId("imgDetailScreenshot").height = (viewPoint.w-30)*orignialRatio;

                var toolTipDivWidth = (viewPoint.w-30-viewPoint.w*0.10)
                domStyle.set("screenshotTooltipDetail", "width", toolTipDivWidth+"px");

                if (eventsModel.cursor.circleX != null)
                {
                    var imageRatio = (viewPoint.w-30)/dom.byId('imgDetailScreenshot').naturalWidth;
                    //alert(dom.byId('imgDetailScreenshot').naturalWidth);
                    domStyle.set("screenshotAnchorDetail", {
                        top: eventsModel.cursor.circleY*imageRatio+'px',
                        left: eventsModel.cursor.circleX*imageRatio+'px',
                        display: ''
                    });

                    domStyle.set("screenshotAnchorInvisibleDetail", {
                        top: eventsModel.cursor.circleY*imageRatio+'px'
                    });

                    if (eventsModel.cursor.circleY > domStyle.get("screenshotTooltipDetail", "height"))
                    {
                        tooltipWidget.show(dom.byId('screenshotAnchorInvisibleDetail'), ['above-centered','below-centered','before','after']);
                        domStyle.set(tooltipWidget.domNode, 'top', (parseInt(domStyle.get(tooltipWidget.domNode, 'top'))+14)+'px');
                    }
                    else
                    {
                        tooltipWidget.show(dom.byId('screenshotAnchorInvisibleDetail'), ['below-centered','below-centered','after','before']);
                        domStyle.set(tooltipWidget.domNode, 'top', (parseInt(domStyle.get(tooltipWidget.domNode, 'top'))-14)+'px');
                    }

                    var pos = domGeom.position("screenshotAnchorDetail", true);
                    var tpLeft = domStyle.get(tooltipWidget.domNode, 'left');
                    domStyle.set(tooltipWidget.anchor, {
                        left: (pos.x-tpLeft+2)+'px'
                    });

                    if (textDataAreaShown)
                    {
                        tooltipWidget.hide();
                    }
                }
                else
                {
                    domStyle.set("screenshotAnchorDetail", "display", "none");
                    tooltipWidget.show(dom.byId('screenshotDefaultAnchorDetail'), ['below-centered','below-centered','after','before']);
                    domStyle.set(tooltipWidget.domNode, 'top', (parseInt(domStyle.get(tooltipWidget.domNode, 'top'))-14)+'px');

                    if (textDataAreaShown)
                    {
                        tooltipWidget.hide();
                    }
                }

            }, 500);
        };

        var setDetailsContext = function (index)
        {
            // only set the cursor if it is different and valid
            var idx = parseInt(index);
            if (idx < eventsModel.model.length)
            {
                //dom.byId("imgDetailScreenshot").onload = dddd;
                eventsModel.set("cursorIndex", idx);
                currentIndex = idx;

                if (idx == 0)
                {
                    domClass.remove("navBtnNext", "navBtnDisabled");
                    domClass.add("navBtnPrevious", "navBtnDisabled");
                }
                else if (idx == (eventsModel.model.length-1))
                {
                    domClass.add("navBtnNext", "navBtnDisabled");
                    domClass.remove("navBtnPrevious", "navBtnDisabled");
                }
                else
                {
                    domClass.remove("navBtnNext", "navBtnDisabled");
                    domClass.remove("navBtnPrevious", "navBtnDisabled");
                }

                var tooltipWidget = registry.byId('textTooltip');
                var viewPoint = win.getBox();

                var toolTipDivWidth = (viewPoint.w-30-viewPoint.w*0.10),
                    pxPerChar = 8,
                    charsPerLine = toolTipDivWidth/pxPerChar;

                if (eventsModel.cursor.annoText.length >= charsPerLine*3)
                {
                    var shortText = eventsModel.cursor.annoText.substr(0, charsPerLine*3-3)+"...";

                    dom.byId('screenshotTooltipDetail').innerHTML = shortText;
                }

                //domStyle.set("imgDetailScreenshot", "width", (viewPoint.w-30)+"px");
                dom.byId("imgDetailScreenshot").width = (viewPoint.w-30);
                domStyle.set("screenshotTooltipDetail", "width", toolTipDivWidth+"px");


                if (eventsModel.cursor.app == null)
                {
                    domStyle.set('editAppNameImg', 'display', '');
                    dom.byId('appNameSpanDetail').innerHTML = "unknown";
                }
                else
                {
                    domStyle.set('editAppNameImg', 'display', 'none');
                }
            }
        };

        var goNextRecord = function()
        {
            if ( (currentIndex+1)< eventsModel.model.length)
            {
                setDetailsContext(currentIndex+1);
            }
        };

        var goPreviousRecord = function()
        {
            if ( (currentIndex-1)>=0)
            {
                setDetailsContext(currentIndex-1);
            }
        };

        var showTextData = function()
        {
            transit(null, dom.byId('textDataAreaContainer'), {
                transition:"slide",
                duration:600
            });

            registry.byId('textTooltip').hide();

            textDataAreaShown = true;
        };

        var hideTextData = function()
        {
            transit(dom.byId('textDataAreaContainer'), null, {
                transition:"slide",
                duration:600,
                reverse:true
            });

            domClass.replace(registry.byId('textTooltip').domNode, "mblTooltipVisible" ,"mblTooltipHidden");

            textDataAreaShown = false;
        };

        var drawOrangeCircle = function()
        {
            var ctx = dom.byId('screenshotAnchorDetail').getContext('2d');
            var canvasWidth = 32;

            ctx.beginPath();
            ctx.strokeStyle = "#FFA500";
            ctx.lineWidth = 3;
            ctx.arc(20, 20, canvasWidth/2, 0, 2 * Math.PI, true);
            ctx.stroke();
            ctx.fillStyle = "rgba(255,165,0, 0.4)";
            ctx.arc(20, 20, canvasWidth/2-3, 0, 2 * Math.PI, true);
            ctx.fill();
        };

        var showAppNameTextBox = function()
        {
            domStyle.set('editAppNameImg', 'display', 'none');

            var pos = domGeom.position('appNameSpanDetail', true);

            domStyle.set('appNameSpanDetail', 'display', 'none');
            domStyle.set('appNameTextBox', {display: '', top:pos.y+'px', left:pos.x+'px'});
            domStyle.set('lightCover', 'display', '');

            window.setTimeout(function(){
                dom.byId('appNameTextBox').click();
                dom.byId('appNameTextBox').focus();
            },300);
            dom.byId('hiddenBtn').focus();

            dom.byId('appNameTextBox').value = dom.byId('appNameSpanDetail').innerHTML == 'unknown'?'':dom.byId('appNameSpanDetail').innerHTML;
        };

        var saveAppName = function()
        {
            var newAppName = dom.byId('appNameTextBox').value.trim();
            dom.byId('hiddenBtn').focus();

            domStyle.set('appNameSpanDetail', 'display', '');
            domStyle.set('appNameTextBox', {display: 'none'});
            domStyle.set('lightCover', 'display', 'none');
            domStyle.set('editAppNameImg', 'display', '');

            dom.byId('appNameSpanDetail').innerHTML = newAppName||'unknown';
        };

        var showToastMsg = function(msg)
        {
            var vp = win.getBox(), msgContainer = dom.byId('toastMsgContainer');

            msgContainer.innerHTML = msg;
            domStyle.set(msgContainer, {
                top: (vp.h-20)/2+'px',
                left: (vp.w-230)/2+'px',
                display:''
            });

            window.setTimeout(function(){
                domStyle.set(msgContainer, {
                    display:'none'
                });
            }, 2000);
        };

        var startX, startY, startX1, startY1;
        return {
            // simple view init
            init:function ()
            {
                eventsModel = this.loadedModels.events;

                _connectResults.push(connect.connect(window, has("ios") ? "orientationchange" : "resize", this, function (e)
                {
                    //adjustSize();
                }));

                _connectResults.push(connect.connect(dom.byId('navBtnNext'), "click", function ()
                {
                    goNextRecord();
                }));

                _connectResults.push(connect.connect(dom.byId('appNameTextBox'), "keydown", function (e)
                {
                    if (e.keyCode == 13)
                    {
                        saveAppName();
                    }
                }));

                _connectResults.push(connect.connect(dom.byId('navBtnPrevious'), "click", function ()
                {
                    goPreviousRecord();
                }));

                _connectResults.push(connect.connect(dom.byId('discussDivDetail'), "click", function ()
                {
                    showTextData();
                }));

                _connectResults.push(connect.connect(dom.byId('addCommentImg'), "click", function ()
                {
                    var text = dom.byId('addCommentTextBox').value.trim();

                    if (!text)
                    {
                        alert('Please enter comment.');
                        dom.byId('addCommentTextBox').focus();
                        return;
                    }

                    eventsModel.cursor.comments.push({author:'unknown', comment:text});

                    dom.byId('addCommentTextBox').value = '';
                    dom.byId('hiddenBtn').focus();
                }));

                _connectResults.push(connect.connect(dom.byId('imgThumbsUp'), "click", function ()
                {
                    if (domClass.contains('imgThumbsUp','icoImgActive'))
                    {
                        domClass.remove('imgThumbsUp', 'icoImgActive');
                    }
                    else
                    {
                        if (domClass.contains('imgFlag','icoImgActive'))
                        {
                            showToastMsg("You must unflag the annotation up.");
                            return;
                        }
                        domClass.add('imgThumbsUp', 'icoImgActive');
                    }
                }));

                _connectResults.push(connect.connect(dom.byId('imgFlag'), "click", function ()
                {
                    if (domClass.contains('imgFlag','icoImgActive'))
                    {
                        domClass.remove('imgFlag', 'icoImgActive');
                    }
                    else
                    {
                        domClass.add('imgFlag', 'icoImgActive');
                    }
                }));

                _connectResults.push(connect.connect(dom.byId('addCommentTextBox'), "keydown", function (e)
                {
                    if (e.keyCode == 13)
                    {
                        var text = dom.byId('addCommentTextBox').value.trim();

                        if (!text)
                        {
                            alert('Please enter comment.');
                            dom.byId('addCommentTextBox').focus();
                            return;
                        }

                        eventsModel.cursor.comments.push({author:'unknown', comment:text});

                        dom.byId('addCommentTextBox').value = '';
                        dom.byId('hiddenBtn').focus();
                    }

                }));

                _connectResults.push(connect.connect(dom.byId('screenshotContainerDetail'), "touchstart", function (e)
                {
                    if( e.touches.length == 1 )
                    {
                        startX = e.touches[0].pageX;
                        startY = e.touches[0].pageY;
                    }
                }));

                _connectResults.push(connect.connect(dom.byId('screenshotContainerDetail'), "touchmove", function (e)
                {
                    if( e.touches.length == 1 )
                    {
                        var endX = e.touches[0].pageX;
                        var endY = e.touches[0].pageY;
                        if ((startX-endX) >=6 &&Math.abs(startY-endY)<10)
                        {
                            dojo.stopEvent(e);
                            showTextData();
                        }
                    }
                }));

                _connectResults.push(connect.connect(dom.byId('textDataAreaContainer'), "touchstart", function (e)
                {
                    if( e.touches.length == 1 )
                    {
                        startX1 = e.touches[0].pageX;
                        startY1 = e.touches[0].pageY;
                    }
                }));

                _connectResults.push(connect.connect(dom.byId('textDataAreaContainer'), "touchmove", function (e)
                {
                    if( e.touches.length == 1 )
                    {
                        var endX1 = e.touches[0].pageX;
                        var endY1 = e.touches[0].pageY;

                        if ((startX1-endX1) <=-6 &&Math.abs(startY1-endY1)<10)
                        {
                            dojo.stopEvent(e);
                            hideTextData();
                        }
                    }
                }));

                _connectResults.push(connect.connect(dom.byId('editAppNameImg'), "click", function ()
                {
                    showAppNameTextBox();
                }));

                drawOrangeCircle();

                dom.byId("imgDetailScreenshot").onload = screenshotImageOnload;
            },
            afterActivate: function()
            {
                if (this.params["cursor"] != null)
                {
                    setDetailsContext(this.params["cursor"]);
                }
                adjustSize();

                domClass.replace(registry.byId('textTooltip').domNode, "mblTooltipVisible" ,"mblTooltipHidden");
                textDataAreaShown = false;
            },
            beforeDeactivate: function()
            {
                registry.byId('textTooltip').hide();
                domStyle.set('textDataAreaContainer', 'display', 'none');
            },
            destroy:function ()
            {
                var connectResult = _connectResults.pop();
                while (connectResult)
                {
                    connect.disconnect(connectResult);
                    connectResult = _connectResults.pop();
                }
            }
        }
    });