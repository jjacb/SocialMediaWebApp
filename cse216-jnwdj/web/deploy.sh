# deploy script for the web front-end

# This file is responsible for preprocessing all TypeScript files, making sure
# all dependencies are up-to-date, and copying all necessary files into the
# web deploy directory.

# This is the resource folder where maven expects to find our files
TARGETFOLDER=../backend/src/main/resources

# This is the folder that we used with the Spark.staticFileLocation command
WEBFOLDERNAME=web

# step 1: make sure we have someplace to put everything.  We will delete the
#         old folder tree, and then make it from scratch
rm -rf $TARGETFOLDER
mkdir $TARGETFOLDER
mkdir $TARGETFOLDER/$WEBFOLDERNAME

# there are many more steps to be done.  For now, we will just copy an HTML file
cp index.html $TARGETFOLDER/$WEBFOLDERNAME
cp login.html $TARGETFOLDER/$WEBFOLDERNAME
cp profile.html $TARGETFOLDER/$WEBFOLDERNAME
cp comment.html $TARGETFOLDER/$WEBFOLDERNAME
cp message.html $TARGETFOLDER/$WEBFOLDERNAME
cp pix/up.jpg $TARGETFOLDER/$WEBFOLDERNAME
cp pix/down.jpg $TARGETFOLDER/$WEBFOLDERNAME
# step 2: update our npm dependencies
npm update

# step 3: copy javascript files
cp node_modules/jquery/dist/jquery.min.js $TARGETFOLDER/$WEBFOLDERNAME
# step 4: compile TypeScript files
node_modules/typescript/bin/tsc app.ts --strict --outFile $TARGETFOLDER/$WEBFOLDERNAME/app.js
node_modules/typescript/bin/tsc login.ts --strict --outFile $TARGETFOLDER/$WEBFOLDERNAME/login.js
node_modules/typescript/bin/tsc profile.ts --strict --outFile $TARGETFOLDER/$WEBFOLDERNAME/profile.js
node_modules/typescript/bin/tsc comment.ts --strict --outFile $TARGETFOLDER/$WEBFOLDERNAME/comment.js
node_modules/typescript/bin/tsc message.ts --strict --outFile $TARGETFOLDER/$WEBFOLDERNAME/message.js

# step 5: copy css files
cp app.css $TARGETFOLDER/$WEBFOLDERNAME
# set up Jasmine
node_modules/typescript/bin/tsc apptest.ts --strict --outFile $TARGETFOLDER/$WEBFOLDERNAME/apptest.js
cp spec_runner.html $TARGETFOLDER/$WEBFOLDERNAME
cp node_modules/jasmine-core/lib/jasmine-core/jasmine.css $TARGETFOLDER/$WEBFOLDERNAME
cp node_modules/jasmine-core/lib/jasmine-core/jasmine.js $TARGETFOLDER/$WEBFOLDERNAME
cp node_modules/jasmine-core/lib/jasmine-core/boot.js $TARGETFOLDER/$WEBFOLDERNAME
cp node_modules/jasmine-core/lib/jasmine-core/jasmine-html.js $TARGETFOLDER/$WEBFOLDERNAME