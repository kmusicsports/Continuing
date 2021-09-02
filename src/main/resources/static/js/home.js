$(function () {
    $('#datePicker').datetimepicker({locale: 'ja', dayViewHeaderFormat: 'YYYY年M月' ,format: 'YYYY/MM/DD'});
    $('#timePicker1').datetimepicker({locale: 'ja', format: 'HH:mm'});
    $('#timePicker2').datetimepicker({locale: 'ja', format: 'HH:mm'});
});


const meetingTab = document.getElementById("pills-meeting-tab");
const accountTab = document.getElementById("pills-account-tab");
const meetingTabContent = document.getElementById("pills-meeting");
const accountTabContent = document.getElementById("pills-account");

if (sessionStorage.getItem("tab") == "account") {
    accountTab.classList.add("active");
    accountTabContent.classList.add("show")
    accountTabContent.classList.add("active")
    meetingTab.classList.remove("active");
    meetingTabContent.classList.remove("show")
    meetingTabContent.classList.remove("active")
} else {
    meetingTab.classList.add("active");
    meetingTabContent.classList.add("show")
    meetingTabContent.classList.add("active")
    accountTab.classList.remove("active");
    accountTabContent.classList.remove("show")
    accountTabContent.classList.remove("active")
}

meetingTab.addEventListener("click", function () {
    sessionStorage.setItem("tab", "meeting");
});

accountTab.addEventListener("click", function () {
    sessionStorage.setItem("tab", "account");
});


$(window).scroll(function () {
  //set scroll position in session storage
  sessionStorage.scrollPos = $(window).scrollTop();
});

var init = function () {
  //return scroll position in session storage
  $(window).scrollTop(sessionStorage.scrollPos || 0);
};

window.onload = init;