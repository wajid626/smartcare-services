


Ext.onReady(function () {

var movie =  Ext.define('appointmentsmodel', {
    extend: 'Ext.data.Model',
        fields: [
           {name: 'PatientName', 	type: 'string'},
           {name: 'PhysicianName', 	type: 'string'},
           {name: 'AppointmentDate',   type: 'string'},
           {name: 'AppointmentTime',   type: 'string'},
           {name: 'Location',   type: 'string'},
           {name: 'CheckInStatus',   type: 'boolean'},
           
          
        ]
    });


 var myStore = Ext.create('Ext.data.Store', {
   model: movie,
   proxy: {
       type: 'ajax',
       url : 'http://smartcare-services.elasticbeanstalk.com/rest/AdminService/findAllAppointments',
       reader: {
           type: 'json',
           root: 'appointments'
       }
   },
   autoLoad: true
});


    Ext.create('Ext.grid.GridPanel', {
        title: 'Appointments',
        store: myStore,
        height: 450,
        width: '100%',
        columns: [{
            text: 'Patient Name',
            dataIndex: 'PatientName',
            width:'20%'
        }, {
            text: 'Physician Name',
            dataIndex: 'PhysicianName',
            width:'20%'
        },
        {
            text: 'Appointment Date',
            dataIndex: 'AppointmentDate',
            width:'15%'
        }, 
        {
            text: 'Appointment Time',
            dataIndex: 'AppointmentTime',
            width:'15%'
        }, 
        {
            text: 'Location',
            dataIndex: 'Location',
            width:'15%'
        }, 
        {
            text: 'Checkin Status',
            dataIndex: 'CheckInStatus',
            width:'15%'
        }
        
        ],
        
        viewConfig: {
		        forceFit:true,
		        fitcontainer:true
        },
        defaults: {
		        flex: 1
      },
        renderTo: Ext.getBody()
    });
});