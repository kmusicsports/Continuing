$(window).scroll(function () {
  //set scroll position in session storage
  sessionStorage.scrollPos = $(window).scrollTop();
});

var init = function () {
  //return scroll position in session storage
  $(window).scrollTop(sessionStorage.scrollPos || 0);
};

window.onload = init;