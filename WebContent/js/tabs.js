/*global Ext:false */
/*

This file is part of Ext JS 4

Copyright (c) 2011 Sencha Inc

Contact:  http://www.sencha.com/contact

GNU General Public License Usage
This file may be used under the terms of the GNU General Public License version 3.0 as published by the Free Software Foundation and appearing in the file LICENSE included in the packaging of this file.  Please review the following information to ensure the GNU General Public License version 3.0 requirements will be met: http://www.gnu.org/copyleft/gpl.html.

If you are unsure which license is appropriate for your use, please contact the sales department at http://www.sencha.com/contact.

*/
Ext.require('Ext.tab.*');

Ext.onReady(function(){	
    // basic tabs 1, built from existing content	
    // second tabs built from JS
	var tabs = Ext.createWidget('tabpanel', {
        renderTo: document.body,
        activeTab: 0,
        width: '100%',
        height: 500,
        plain: true,
        defaults :{
            autoScroll: false,
            bodyPadding: 0
        },
        items: [{
                title: 'Appointments',
                id :'Appointments',
                loader: {
                    url: 'appointments-tab.html',
                    contentType: 'html',
                    autoLoad: true
               
                }
            },{
            	id : 'Payments',
                title: 'Payments',
                loader: {
                    url: 'payments-tab.html',
                    contentType: 'html',
                    autoLoad: true
                },
            },{
                id : 'Beacons',
            	title: 'Beacons',
                loader: {
                    url: 'beacons-tab.html',
                    contentType: 'html',
                },
                listeners: {
                	activate: function(tabs) {
                		tabs.loader.load();
                	}
                }
            
            }
        ]
    });
	
});
