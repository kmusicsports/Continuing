const followsLink = document.getElementById("follows-link");
const followersLink = document.getElementById("followers-link");

followsLink.addEventListener("click", function () {
    sessionStorage.setItem("tab", "follows");
});

followersLink.addEventListener("click", function () {
    sessionStorage.setItem("tab", "followers");
});