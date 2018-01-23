macro "Treat_RFP"{

    Argument = getArgument();
    Arguments = split(Argument, "\t");

    OUTFolder = Arguments[0];
    myImageName = Arguments[1];
    MinSizePLA = parseFloat(Arguments[2]);
    MaxSizePLA = parseFloat(Arguments[3]);

    //Color map for drawing
    //Red
    CmapR = newArray(31, 255, 44, 214, 148, 140, 227, 225, 188, 23);
    //Green
    CmapG = newArray(119, 127, 160, 39, 103, 86, 119, 0, 189, 190);
    //Blue
    CmapB = newArray(180, 14, 44, 40, 189, 75, 194, 225, 34, 207);


    //get The number of found Nuclei
    Nnuclei = roiManager("count");

    //Path to Nuclei ROIset
    PathNUCset = OUTFolder;
    PathNUCset += myImageName + "_Nuclei.zip";

    //reset to create the set of Nuclei and PLA
    roiManager("Reset");
    PathPLAset = OUTFolder;
    PathPLAset += myImageName + "_Nuclei-PLA.zip";


    Windows = newArray("RFP", "RFPori");

    for (w=0; w<Windows.length; w++){
        selectWindow(Windows[w]);
        //Remove Background
        run("Subtract Background...", "rolling=100");
        //Enhance Contrast
        run("Enhance Contrast", "saturated=0.35");
    }

    //Prepare window RFP2 for immediate drawing of the nuclei and PLA
    selectWindow("RFP2");
    run("RGB Color");


    for (nucleus = 0; nucleus<Nnuclei; nucleus++){

        selectWindow("RFP");

        //Get Current Nucleus properties
        roiManager("Reset");
        roiManager("Open", PathNUCset);
        roiManager("Select", nucleus);
        myNucleusName = Roi.getName();
        infos = split(myNucleusName, " ");
        index = parseFloat(infos[1]) - 1;

        //Get an index between 0-9 whatever value
        index = ((index / 10) - floor(index / 10)) * 10;

        //Get Nucleus coordinates to transfer to final ROIset
        roiManager("Select", nucleus);
        Roi.getCoordinates(NUCXpoints, NUCYpoints);

        //Get the corresponding Cmap value (RGB)
        setForegroundColor(CmapR[index],
                            CmapG[index],
                            CmapB[index]);
        run("Line Width...", "line=2");


        //Update the NUclei and PLA ROIset
        roiManager("Reset");
        if (File.exists(PathPLAset)==1){
            roiManager("Open", PathPLAset);
        }

        nRoi = roiManager("count");

        //add the current nucleus
        makeSelection("polygon", NUCXpoints, NUCYpoints);
        roiManager("Add");
        roiManager("Select", nRoi);
        roiManager("Rename", myNucleusName);

        //Threshold the image with the pixel values of the current nucleus
        roiManager("Select", nRoi);
        setAutoThreshold("Moments dark");
        //setAutoThreshold("IsoData dark");

        //Find all PLA labbelling
        CMD1 = "size=" + MinSizePLA + "-" + MaxSizePLA;
        CMD1 += " show=Nothing";
        //The found Particles are added to the roiManager
        CMD1 += " add";
        run("Analyze Particles...", CMD1);

        //Draw the Particles
        selectWindow("RFP2");

        roiManager("Select", nRoi);
        run("Draw");

        nPLA = 0;
        for (pla = nRoi + 1; pla < roiManager("count"); pla++){
            nPLA += 1;
            roiManager("Select", pla);
            roiManager("Rename", "   PLA " + nPLA + " N" + infos[1]);
            run("Draw");
        }

        //Save new ROI
        roiManager("Save", PathPLAset);
        roiManager("Reset");

    }

    /*

    run("Analyze Particles...", "size=5-Infinity exclude add");
    */

    //Restore RFP image and make it Red and RGB
    selectWindow("RFP");
    resetThreshold();

    for (w=0; w<Windows.length; w++){
        selectWindow(Windows[w]);
        run("Red");
        run("RGB Color");
    }


    //Draw all nuclei on RFP
    for(nucleus=0;
        nucleus<roiManager("count");
        nucleus++){

        selectWindow("RFP");
        roiManager("Select", nucleus);
        /*
            Fitting with "Fit Elipse" does not give better results...
            2018/01/22
        */
        roiManager("Rename", "NUCLEUS " + (nucleus+1));

        //Get an index between 0-9 whatever value
        index = ((nucleus / 10) - floor(nucleus / 10)) * 10;

        //Get the corresponding Cmap value (RGB)
        setForegroundColor(CmapR[index],
                            CmapG[index],
                            CmapB[index]);

        //Draw the nucleus on DAPI
        run("Line Width...", "line=2");
        run("Draw");
    }

}
