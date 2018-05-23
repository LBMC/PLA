Automated Detection of PLA
===


Introduction
---

The `PLA` macro for the program ImageJ ([Schneider et al. 2012](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC5554542/)) is based on a previously released macro ([NUKE-BREAK](https://github.com/LBMC/NUKE-BREAK), [Herbette et al. 2017](https://www.sciencedirect.com/science/article/pii/S1568786417302707?via%3Dihub)) and optimized to detect **Proximity Ligation Assay** foci within nuclei of human cell. The macro was designed to automatically detect and process batches of microscope coupled acquisitions. To this end, key parameters for nuclei and foci detection (minimum and maximum surface and circularity) have been optimized.


The `PLA` macro explores recursively a specific folder and treat all images with the correct extension (chosen by the user).

|![Example original](doc/Original.jpg)|![Example treated](doc/Treated.jpg)|
|-------------------------------------|-----------------------------------|
|**ORIGINAL**   |**TREATED**   |

The macro will generate a listing of couples of acquisition images using user defined `DAPI.extension` and `PLA.extension`.
Using the GUI, the user can specify the `extensions` as the main analysis parameters:
- Minimum Surface for initial nuclei detection
- Maximum Surface for initial nuclei detection
- Maximum Surface of a single nucleus (for nuclei aggregates identification and splitting)
- Minimum Circularity of single nucleus (for nuclei aggregates identification and splitting)
- Minimum Surface for PLA foci
- Maximum Surface for PLA foci


**These parameters can be changed and saved, so the program can be adapted to specific acquisition resolution and/or experimental conditions affecting the geometry of the nuclei and the PLA foci.**

The images couples are then processed as follows:
1. The noise of both `DAPI` and `PLA` images is independently removed using the [“substract background” function](http://ieeexplore.ieee.org/document/1654163/?reload=true).
2. The `DAPI` channel is then used to detect the Nuclei using the ["Huang" thresholding method](https://pdfs.semanticscholar.org/8906/64d6e7861253bd8c36d0e9079f96c9f22d67.pdf) and the **initial Nuclei** `Minimum surface` and `Maximum surface`  parameters.
3. The aggregates of nuclei are subsequently identified using the **single Nucleui** `Maximum surface` and `Minimum Circularity`. Detected ROI fitting with these parameters are then splitted using the ["Watershed" algorithm](https://www.spiedigitallibrary.org/conference-proceedings-of-spie/1360/1/Determining-watersheds-in-digital-pictures-via-flooding-simulations/10.1117/12.24211.short?SSO=1).
4. `PLA` foci are then detected independently for every **isolated nucleus**, using the [“Max-Entropy” threshold method](https://www.sciencedirect.com/science/article/pii/0734189X85901252) and the foci `Minimum Surface` and `Maximum Surface` parameters.
5. Finally, the program collects the size of all nuclei and foci to generate a `Results.csv` table (see below).


Input
---
For each aquistion 2 files should be provided:
- One DAPI (or any other nucleus labelling) staining.
- One PLA staining

All DAPI files should have the same explicit extension (e.g. `_w1DAPI.TIF`). It should be the same for the PLA files (e.g. `_w2RFP.TIF`). Only DAPI files with a corresponding PLA file will be treated.

As the program is able to treat files as batch, all images must have the same resolution.

Output
---  
The macro generate several files and folders that have all the same Fingerprint (Date and time).
- 1 `yyyy-mm-dd_hh-mm_Parameters_and_Files.txt` file in the root folder indicated by the user. This file contains all parameters used for the analysis, and the list of all the picture files that the macro will treat.
- 1 `yyyy-mm-dd_hh-mm_Results.csv` table file containing the properties of all Nuclei and PLA Foci detected for all images. The data are presented in the following columns:
    - `File`: File name.
    - `Nuclei`: Names of the Nuclei in the file.
    - `Surface`: Surface in pixels of the Nuclei.
    - `PLA foci`: Names of PLA Foci in one Nucleus.
    - `Surface PLA`: Total PLA surface in a Nucleus or Surface of each PLA Foci.
    - `% of Nucleus surface`: Percentage of the surface of a Nucleus covered by the total PLA Foci, or by each PLA Foci.
- 1 `yyyy-mm-dd_hh-mm_ImageName` folder containing:
    - 1 `ImageName_Bilan.jpg` file showing the identified Nuclei and PLA Foci on the original DAPI and PLA stainings.
    - 1 `ImageName_Nuclei.jpg` file showing the identified Nuclei on the original DAPI staining.
    - 1 `ImageName_PLA.jpg` file showing the identified Nuclei and associated PLA Foci on the original PLA staining.
    - 1 `ImageName_Original.jpg` file presenting the overlay of original DAPI and PLA stainings input.
    - 1 `ImageName_Nuclei.zip` ROIset file for `ImageJ`. This is the annotated ROIs of the detected Nuclei.
    - 1 `ImageName_Nuclei-PLA.zip` ROIset file for `ImageJ`. This is the annotated ROIs of the detected Nuclei directly associated with the ROIs of PLA Foci they contain.



**Contributors**
--

| ![LBMC Logo](doc/Logo_LBMC.jpg) ![CNRS Logo](doc/Logo_cnrs.jpg) ![ENS Logo](doc/Logo_ens.jpg) ||
|-----------------------------|------------|
|**CLUET David**|     [david.cluet@ens-lyon.fr](david.cluet@ens-lyon.fr)|
|**TERRONE Sophie**|     [sophie.terrone@ens-lyon.fr](sophie.terrone@ens-lyon.fr)|


**Publication**
--


**THE RNA HELICASE DDX17 CONTROLS THE TRANSCRIPTIONAL
ACTIVITY OF REST AND THE EXPRESSION OF PRONEURAL
MICRORNAS IN NEURONAL DIFFERENTIATION.**

Marie-Pierre Lambert, Sophie Terrone, Guillaume Giraud, Clara Benoit-Pilven, David
Cluet, Valérie Combaret, Franck Mortreux, Didier Auboeuf and Cyril F.Bourgeois

**Nucleic Acids Research** (under revision)


**Requirements**
--
The `PLA` macro requires `ImageJ v1.49g` or higher ([Download](https://imagej.nih.gov/ij/download.html)).

**Files**
--
- [] **src**
    - README.md
    - LICENSE
    - `Installation.ijm`
    - `Installation_FIJI.ijm`
    - [] **doc**
        - *FIJI.jpg*
        - *IJ.jpg*
        - *Logo_cnrs.jpg*
        - *Logo_ens.jpg*
        - *Logo_LBMC.jpg*
        - *Original.jpg*
        - *Treated.jpg*
    - [] **macro**
        - `Close_Image.java`
        - `Command_Line.txt`
        - `Explorer.java`
        - `GUI.java`
        - `Main.java`
        - `Settings.txt`
        - `Treat_DAPI.java`
        - `Treat_RFP.java`


**Installation**
--
The `PLA` macro requires can be automatically installed with all required files in `ImageJ` and `FIJI`. Please follow the specific instructions described below.

![ImageJ Logo](doc/IJ.jpg)
---
1. Open `ImageJ`.
2. Open the `src` folder of the `PLA` macro.
3. Drag the `Installation.ijm` file on `ImageJ` Menu bar to open it.
4. In the Menu bar of the macro select the `Macros/Run Macro` option.
5. The window will be closed automatically and all required files will be installed in the `ImageJ/macros/PLA` folder. The shortcut `Plugins/Macros/PLA` will be added in the Menu bar.
6. Restart `ImageJ` to refresh the Menu bar.

![FIJJ Logo](doc/FIJI.jpg)
---
1. Open `FIJI`.
2. Open the `src` folder of the `PLA` macro.
3. Drag the `Installation_Fiji.ijm` file on `FIJI` Menu bar to open it.
4. In the console select the `Run` option.
5. All required files will be installed in the `Fiji.app/macros/PLA` folder. The shortcut `Plugins/Macros/PLA` will be added in the Menu bar.
6. Restart `FIJI` to refresh the Menu bar.

Update
---
Follow the same instructions as for the installation process. As the settings for the detection of the nuclei and the PLA foci can be modified by the user, the `Settings.txt` file will not be affected. The `Your setting file has been protected!` message will indicate your analysis parameters have been preserved.
