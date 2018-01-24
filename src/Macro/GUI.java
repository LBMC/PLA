macro "GUI"{

    //Path of the folder containing all scripts
    PathMACRO = getDirectory("macros")+"PLA"+File.separator;

    //retrieve arguments
    Argument = getArgument();
    Arguments = split(Argument, "\t");

    PathFolderInput = Arguments[0];
    FP = Arguments[1];

    //Retrrieve the updated (if so) Parameters in the SEttings.txt file
    P = File.openAsString(PathMACRO+"Settings.txt");

    //Create Array of lines
    Params = split(P, "\n");

    //Get all single parmaters
    MinSize = parseFloat(getParm(Params, 0, 1));
    MaxSize = getParm(Params, 1, 1);
    if (MaxSize!="Infinity"){
        MaxSize = parseFloat(MaxSize);
    }
    MaxSizeSingle = parseFloat(getParm(Params, 2, 1));
    MinCircSingle = parseFloat(getParm(Params, 3, 1));
    MinSizePLA = parseFloat(getParm(Params, 4, 1));
    MaxSizePLA = getParm(Params, 5, 1);
    if (MaxSizePLA!="Infinity"){
        MaxSizePLA = parseFloat(MaxSizePLA);
    }
    ExtDAPI = getParm(Params, 6, 1);
    ExtRFP = getParm(Params, 7, 1);

    //Get descriptions for automated detection of change and updating of the
    //Settings.txt file
    DesMinSize = "" + getParm(Params, 0, 0) + "=";
    DesMaxSize = "" + getParm(Params, 1, 0) + "=";
    DesMaxSizeSingle = "" + getParm(Params, 2, 0) + "=";
    DesMinCircSingle = "" + getParm(Params, 3, 0) + "=";
    DesMinSizePLA = "" + getParm(Params, 4, 0) + "=";
    DesMaxSizePLA = "" + getParm(Params, 5, 0) + "=";
    DesExtDAPI = "" + getParm(Params, 6, 0) + "=";
    DesExtRFP = "" + getParm(Params, 7, 0) + "=";


/*
===============================================================================
                            GRAPHIC INTERFACE
===============================================================================
*/


    Dialog.create("PLA DETECTION PARAMETERS");

    Dialog.addMessage("Parameters for the detection of the Nuclei");
    Dialog.addString(DesExtDAPI, ExtDAPI);
    Dialog.addString(DesMinSize, "" + MinSize);
    Dialog.addString(DesMaxSize, "" + MaxSize);
    Dialog.addString(DesMaxSizeSingle, "" + MaxSizeSingle);
    Dialog.addString(DesMinCircSingle, "" + MinCircSingle);

    Dialog.addMessage("\nParameters for the detection of the PLA Foci");
    Dialog.addString(DesExtRFP, ExtRFP);
    Dialog.addString(DesMinSizePLA, "" + MinSizePLA);
    Dialog.addString(DesMaxSizePLA, "" + MaxSizePLA);

    Dialog.show();

    //Update the parameters
    ExtDAPI = Dialog.getString();
    MinSize = Dialog.getString();
    MaxSize = Dialog.getString();
    MaxSizeSingle = Dialog.getString();
    MinCircSingle = Dialog.getString();

    ExtRFP = Dialog.getString();
    MinSizePLA = Dialog.getString();
    MaxSizePLA = Dialog.getString();

    newP = DesMinSize + MinSize + "\n";
    newP += DesMaxSize + MaxSize + "\n";
    newP += DesMaxSizeSingle + MaxSizeSingle + "\n";
    newP += DesMinCircSingle + MinCircSingle + "\n";
    newP += DesMinSizePLA + MinSizePLA + "\n";
    newP += DesMaxSizePLA + MaxSizePLA + "\n";
    newP += DesExtDAPI + ExtDAPI + "\n";
    newP += DesExtRFP + ExtRFP + "\n";


/*
===============================================================================
                        DETECTION OF ALL CORRECT IMAGES
===============================================================================
*/

    //Create the parameters Files
    myAnalysis = "" + FP + "Parameters_and_Files.txt";
    f = File.open(PathFolderInput + myAnalysis);
    File.close(f);

    //Update Parameters Files
    File.append("***PARAMATERS***",
                PathFolderInput + myAnalysis);
    File.append(newP + "\n" + "***FILES***",
                PathFolderInput + myAnalysis);

    myText = File.openAsString(PathFolderInput + myAnalysis);

    //Detect all valid FILES
    ARG1 = PathFolderInput + "\t";
    ARG1 += ExtDAPI + "\t";
    ARG1 += ExtRFP + "\t";
    ARG1 += PathFolderInput + myAnalysis;

    runMacro(PathMACRO + "Explorer.java",
                ARG1);

    //Extract the Files Names
    ResExplorer = File.openAsString(PathFolderInput + myAnalysis);
    Segments = split(ResExplorer, "*");
    myFiles = split(Segments[Segments.length-1],"\n");

    //Due to \n added by append, first file is on index 1
    NameFiles = newArray();
    for (i=1; i<myFiles.length; i++){
        currentF = File.getName(myFiles[i]);
        currentF = replace(currentF, ExtDAPI, "");
        NameFiles = Array.concat(NameFiles, currentF);
    }

    myChoices = newArray("Keep my original settings",
                            "Update my settings");

    //Display the result of the research and propose to update parameters
    Dialog.create("DETECTED FILES");
    MSG = "" + (myFiles.length-1) + " files have been detected.\n";
    MSG += "See listing below:";
    Dialog.addMessage(MSG);

    if (NameFiles.length>0){
        Dialog.addChoice("Files:", NameFiles, NameFiles[0]);
    }

    if (newP != P){
        MSG1 = "Your settings are differents from Settings.txt" +"\n";
        MSG1 += "Choose the below update option to definitively\n";
        MSG1 += "change the Settings.txt file.\n\n";
        MSG1 += "The current modified settings will be used anyway for this analysis.";
        Dialog.addMessage(MSG1);
        Dialog.addChoice("", myChoices, myChoices[0]);
    }
    Dialog.show();

    if (NameFiles.length>0){
        fileN = Dialog.getChoice();
    }
    
    if (newP != P){
        option = Dialog.getChoice();
        if (option == "Update my settings"){
            //Update the SEttings.txt file for next analysis.
            mySettings = File.open(PathMACRO + "Settings.txt");
            print(mySettings, newP);
            File.close(mySettings);
        }
    }

/*
===============================================================================
                            FUNCTIONS
===============================================================================
*/

function getParm(Params, index, what){
    //Split the line index of Settings.txt file at the "=" sign
    infos = split(Params[index], "=");
    //Return the value [1] or the description of the variable [0]
    return infos[what];
}

}//END MACRO
