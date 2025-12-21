$(document).ready(function () {
    $('.page-link').on('click', function (e) {
        e.preventDefault();
        var url = $(this).attr('href');
        loadPageContent(url);
    });

    function loadPageContent(url) {
        $('#itemContainer').load(url + ' #itemContainer > *', function (response, status, xhr) {
        });
    }
});
