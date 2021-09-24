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
