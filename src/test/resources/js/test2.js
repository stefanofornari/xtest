var ret = {};

$.mockjax({
  url: '/xtest/mock1',
  responseText: {
    name: 'ste'
  }
});

$.ajax({
    url: '/xtest/mock1',
    type: "GET",
    cache: false,
    dataType: "json",
    success: function(response) {
        ret = { status: "success", name: response.name };
    },
    error: function(qXHR, textStatus, errorThrown) {
        print(textStatus + ": " + errorThrown);
    }
});

