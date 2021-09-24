$(function () {
    $('#datePicker').datetimepicker({locale: 'ja', dayViewHeaderFormat: 'YYYY年M月' ,format: 'YYYY/MM/DD'});
    $('#startTimePicker').datetimepicker({locale: 'ja', format: 'HH:mm'});
    $('#endTimePicker').datetimepicker({locale: 'ja', format: 'HH:mm'});
});