const followsTab = document.getElementById("pills-follows-tab");
const followersTab = document.getElementById("pills-followers-tab");
const followsTabContent = document.getElementById("pills-follows");
const followersTabContent = document.getElementById("pills-followers");

if (sessionStorage.getItem("tab") == "followers") {
    followersTab.classList.add("active");
    followersTabContent.classList.add("show")
    followersTabContent.classList.add("active")
    followsTab.classList.remove("active");
    followsTabContent.classList.remove("show")
    followsTabContent.classList.remove("active")
} else {
    followsTab.classList.add("active");
    followsTabContent.classList.add("show")
    followsTabContent.classList.add("active")
    followersTab.classList.remove("active");
    followersTabContent.classList.remove("show")
    followersTabContent.classList.remove("active")
}

followsTab.addEventListener("click", function () {
    sessionStorage.setItem("tab", "follows");
});

followersTab.addEventListener("click", function () {
    sessionStorage.setItem("tab", "followers");
});