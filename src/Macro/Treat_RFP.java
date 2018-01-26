macro "Treat_RFP"{

    Argument = getArgument();
    Arguments = split(Argument, "\t");

    OUTFolder = Arguments[0];
    myImageName = Arguments[1];
    MinSizePLA = parseFloat(Arguments[2]);
    MaxSizePLA = parseFloat(Arguments[3]);
    PathReport = Arguments[4];
    myProgress = parseFloat(Arguments[5]);

    //Color map for drawing
    //Red
    CmapR = newArray(31, 255, 44, 214, 148, 140, 227, 225, 188, 23);
    //Green
    CmapG = newArray(119, 127, 160, 39, 103, 86, 119, 0, 189, 190);
    //Blue
    CmapB = newArray(180, 14, 44, 40, 189, 75, 194, 225, 34, 207);

    //Path to Nuclei ROIset
    PathNUCset = OUTFolder;
    PathNUCset += myImageName + "_Nuclei.txt";

    //Path to Nuclei ROIset
    PathNUCsetzip = OUTFolder;
    PathNUCsetzip += myImageName + "_Nuclei.zip";

    //Create the set of Nuclei and PLA
    PathPLAset = OUTFolder;
    PathPLAset += myImageName + "_Nuclei-PLA.txt";

    //Create the set of Nuclei and PLA
    PathPLAsetzip = OUTFolder;
    PathPLAsetzip += myImageName + "_Nuclei-PLA.zip";



    roiManager("Reset");
    ROIopen(PathNUCset);

    //get The number of found Nuclei
    Nnuclei = roiManager("count");
    roiManager("Reset");


    Windows = newArray("RFP", "RFPori");

    for (w=0; w<Windows.length; w++){
        showProgress(myProgress);
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
        showProgress(myProgress);
        selectWindow("RFP");

        //Get Current Nucleus properties
        roiManager("Reset");
        ROIopen(PathNUCset);
        roiManager("Select", nucleus);
        myNucleusName = Roi.getName();
        List.setMeasurements;
        SN = List.getValue("Area");

        //Update Report
        myInfo = ""+ "\t" + myNucleusName + "\t";
        myInfo += "" + SN + "\t" + "FOCI" + "\t" + "SURF" + "\t" + "PERC";

        File.append(myInfo,
                   PathReport);

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
            ROIopen(PathPLAset);
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
        STot = 0;
        for (pla = nRoi + 1; pla < roiManager("count"); pla++){
            showProgress(myProgress);
            nPLA += 1;
            roiManager("Select", pla);
            List.setMeasurements;
            S = List.getValue("Area");
            STot += S;
            Perc = d2s(100*(S/SN), 2);
            NomPLA = "PLA " + nPLA;

            //Update Report
            File.append("\t\t\t" + NomPLA + "\t" + S + "\t" + Perc,
                       PathReport);

            roiManager("Rename", "   PLA " + nPLA + " N" + infos[1]);
            run("Draw");
        }
        PercTot = d2s(100*(STot/SN), 2);

        //Save new ROI
        ROIsave(PathPLAset, "overwrite");
        //roiManager("Save", PathPLAset);

        roiManager("Reset");

        //Update the Nucleus line
        myReport = File.openAsString(PathReport);
        myReport = replace(myReport, "SURF", "" + STot);
        myReport = replace(myReport, "FOCI", "" + nPLA);
        myReport = replace(myReport, "PERC", "" + PercTot);
        f = File.open(PathReport);
        File.close(f);
        File.append(myReport,
                   PathReport);
    }

    /*

    run("Analyze Particles...", "size=5-Infinity exclude add");
    */

    //Restore RFP image and make it Red and RGB
    selectWindow("RFP");
    resetThreshold();

    for (w=0; w<Windows.length; w++){
        showProgress(myProgress);
        selectWindow(Windows[w]);
        run("Red");
        run("RGB Color");
    }


    //Draw all nuclei on RFP
    ROIopen(PathNUCset);

    for(nucleus=0;
        nucleus<roiManager("count");
        nucleus++){
        showProgress(myProgress);
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

    //Clean the mess with the ROI Files

    roiManager("Save", PathNUCsetzip);
    roiManager("reset");

    ROIopen(PathPLAset);
    roiManager("Save", PathPLAsetzip);
    roiManager("reset");

    d = File.delete(PathNUCset);
    d = File.delete(PathPLAset);
    showProgress(myProgress);


/*
===============================================================================
                            FUNCTIONS
===============================================================================
*/

function ROIopen(path){
    T = File.openAsString(path);

    //Separate the ROI
    ROI = split(T, "\n");

    for(roi=0; roi<ROI.length; roi++){
        showProgress(myProgress);
        segments = split(ROI[roi], "*");
        Nom = segments[0];
        xpoints = split(segments[1], ";");
        ypoints = split(segments[2], ";");
        makeSelection("polygon", xpoints, ypoints);
        roiManager("Add");
        roiManager("Select", roiManager("count")-1);
        roiManager("Rename", Nom);
    }
}

/*
===============================================================================
*/

function ROIsave(path, option){
    /*
        Recognized options:
        -"overwrite"
        -"append"

        Structure of the roi:
        "Name*X0;X1;...;Xn*Y0;Y1;...;Yn"
    */
    //create the file if not existing
    if(File.exists(path)==0){
        f = File.open(path);
        File.close(f);
    }

    //clear the file if overwriting is ordered
    if(option=="overwrite"){
        f = File.open(path);
        File.close(f);
    }

    //Loop of saving the ROIs
    for(roi=0; roi<roiManager("count"); roi++){
        showProgress(myProgress);
        roiManager("Select", roi);
        Roi.getCoordinates(xpoints, ypoints);
        Nom = Roi.getName();

        //Convert coordinates arrays as a single string
        X = "";
        Y = "";

        for(x=0; x<xpoints.length; x++){
            X += "" + xpoints[x];
            Y += "" + ypoints[x];

            if(x!=xpoints.length-1){
                X += ";";
                Y += ";";
            }
        }

        //Append the roi to the file
        File.append(Nom + "*" + X + "*" + Y,
                    path);
    }
}



}//END MACRO
