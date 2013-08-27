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
    "dojox/mvc/at",
    "dojo/store/Memory",
    "dojox/mvc/getStateful"
],
    function (arrayUtil, dom, domClass, domGeom, domStyle, query, lang, connect, win, has, sniff, registry, at, Memory, getStateful)
    {
        var _connectResults = []; // events connect results
        var eventsModel = null;
        var app = null;

        var loadListData = function ()
        {
            for (var i = 0, l = 5; i < l; i++)
            {
                var eventData = {
                    "annoText": "annoText a"+i,
                    "app": "app a" + i,
                    "author": "author a" +i,
                    "screenshot":"demodata/screenshots/sc4.png",
                    deviceInfo:"Nexus 4, Android 4.3",
                    comments:[]
                };

                eventsModel.store.add(eventData);
                eventsModel.model.push(new getStateful(eventData));
            }
        };

        var loadMoreData = function()
        {
            for (var i = 0, l = 5; i < l; i++)
            {
                var eventData = {
                    "annoText": "annoText a"+i,
                    "app": "app a" + i,
                    "author": "author a" +i,
                    "screenshot":"demodata/screenshots/sc4.png",
                    deviceInfo:"Nexus 4, Android 4.3",
                    comments:[]
                };

                eventsModel.store.add(eventData);
                eventsModel.model.push(new getStateful(eventData));
            }

            adjustSize();
        };

        var adjustSize = function()
        {
            var viewPoint = win.getBox();
            var parentBox = domGeom.getMarginBox("headingStart");

            domStyle.set("listContainerStart", "height", (viewPoint.h-parentBox.h)+"px");
        };

        return {
            // simple view init
            init:function ()
            {
                console.log("console from view", this);
                eventsModel = this.loadedModels.events;
                app = this.app;

                _connectResults.push(connect.connect(dom.byId('btnLoadListData'), "click", function ()
                {
                    loadListData();
                }));

                _connectResults.push(connect.connect(window, has("ios") ? "orientationchange" : "resize", this, function (e)
                {
                    adjustSize();
                }));

                _connectResults.push(connect.connect(dom.byId('listContainerStart'), "scroll", this, function(){
                    var toEnd = false;
                    var listContainer = dom.byId('listContainerStart');
                    if ((listContainer.clientHeight + listContainer.scrollTop) >= listContainer.scrollHeight) toEnd = true;

                    if (toEnd)
                    {
                        loadMoreData();
                    }
                }));
            },
            afterActivate: function()
            {
                adjustSize();
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