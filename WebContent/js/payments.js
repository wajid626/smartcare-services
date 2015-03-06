

Ext.onReady(function () {
var rental =  Ext.define('rentalmodel', {
    extend: 'Ext.data.Model',
        fields: [
           {name: 'PatientName', 	type: 'string'},
           {name: 'PhysicianName', 	type: 'string'},
           {name: 'BilledAmount',   type: 'string'},
           {name: 'PaymentStatus', 	type: 'string'}
      ]
    });

   
 var myStore = Ext.create('Ext.data.Store', {
   model: rental,
   proxy: {
       type: 'ajax',
       url : 'http://smartcare-services.elasticbeanstalk.com/rest/AdminService/findPaymentDetails',
       reader: {
           type: 'json',
           root: 'payments'
       }
   },
   autoLoad: true
});
   
 
 Ext.create('Ext.grid.GridPanel', {
     title: 'Payments',
     store: myStore,
     height: 450,
     width: '100%',
     columns: [{
         text: 'Patient Name',
         dataIndex: 'PatientName',
         width:'15%'
     }, {
         text: 'Physician Name',
         dataIndex: 'PhysicianName',
         width:'15%'
     },{
         text: 'Billed Amount',
         dataIndex: 'BilledAmount',
         width:'20%'
     },{
         text: 'Payment Status',
         dataIndex: 'PaymentStatus',
         width:'10%'
     }],
     
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