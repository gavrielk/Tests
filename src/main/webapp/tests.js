$(function() {
    setTestsCheckboxs();
});

function setTestsCheckboxs()
{
    var profileName = $('#profile').find(":selected").text();
    $("[type=checkbox").prop("checked", false);
    if (profileName !== null && profileName !== "")
    {
        it.doGetTestsInProfile(profileName, function(t) {
            console.log(t.responseObject());
            $(t.responseObject()).each(function(index) {
                console.log( index + ": " + this );
                $("[name=test_" + this).prop("checked", true);
            });
        }); 
    }
}