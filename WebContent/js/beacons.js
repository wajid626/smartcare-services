


Ext.onReady(function () {
	
var user =  Ext.define('usermodel', {
    extend: 'Ext.data.Model',
        fields: [
           {name: 'BeaconId', 		type: 'string'},
           {name: 'Location', 		type: 'string'},
           {name: 'Status',   		type: 'string'},
           {name: 'BatteryLevel',   type: 'string'}
        ]
    });

 var myStore = Ext.create('Ext.data.Store', {
   model: user,
   proxy: {
       type: 'ajax',
       url : 'http://smartcare-services.elasticbeanstalk.com/rest/AdminService/findBeaconDetails',
       reader: {
           type: 'json',
           root: 'beacons'
       }
   },
   autoLoad: true
});
   
    Ext.create('Ext.grid.GridPanel', {
        title: 'Beacons	',
        width:'100%',
        store: myStore,
        columns: [{
        	width: '20%',
            text: 'Beacon Id',
            dataIndex: 'BeaconId'
        }, {
            text: 'Location',
            dataIndex: 'Location',
        	width: '20%'
        },
        {
            text: 'Status',
            dataIndex: 'Status',
        	width: '20%'
        }, {
            text: 'BatteryLevel',
            dataIndex: 'BatteryLevel',
        	width: '9%'
        }],
        height: 500,
        renderTo: Ext.getBody()
    });
});