// var describe: any;
// var it: any;
// var expect: any;
// describe("Tests of basic math functions", function() {
//     it("Adding 1 should work", function() {
//         var foo = 0;
//         foo += 1;
//         expect(foo).toEqual(1);
//     });

//     it("Subtracting 1 should work", function () {
//         var foo = 0;
//         foo -= 1;
//         expect(foo).toEqual(-1);
//     });
//     it("UI Test: Add Button Hides Listing", function(){
//         // click the button for showing the add button
//         ($('#showFormButton')).click();
//         // expect that the add form is not hidden
//         expect($("#addElement").attr("style").indexOf("display: none;")).toEqual(-1);
//         // expect tha tthe element listing is hidden
//         expect($("#showElements").attr("style").indexOf("display: none;")).toEqual(0);
//         // reset the UI, so we don't mess up the next test
//         $('#addCancel').click();        
//     });
//     it("UI Test: Cancel button in add a new entry", function(){
//         // click the button for showing the add button
//         ($('#showFormButton')).click();
//         // expect that the add form is not hidden
//         expect($("#addElement").attr("style").indexOf("display: none;")).toEqual(-1);
//         // reset the UI, so we don't mess up the next test
//         $('#addCancel').click();
//         //ensure the cancel button returns us to the main page (checks that the add form button is back)
//         expect($("#addElement").attr("style").indexOf("display: none;")).toEqual(0);
//     }); 
// });
