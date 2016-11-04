
WEIGHT BY weights.
EXECUTE.
 
*********I6_2  

if(M2_99=99) M2_99code=1.
exe.

Recode M2_99code (sysmis=0).	
EXECUTE.

do IF QI1=1 and M2_99code=0 and QI20_5=1.
Recode I6_2_5_71 I6_2_5_72 I6_2_5_73 I6_2_5_98 I6_2_5_99   (sysmis=0).	
End if.
EXECUTE.

do IF QI1=1 and M2_99code=0 and QI20_7=1.
Recode I6_2_7_71 I6_2_7_72 I6_2_7_73 I6_2_7_98 I6_2_7_99   (sysmis=0).	
End if.
EXECUTE.

do IF QI1=1 and M2_99code=0 and QI20_8=1.
Recode I6_2_8_71 I6_2_8_72 I6_2_8_73 I6_2_8_98 I6_2_8_99   (sysmis=0).	
End if.
EXECUTE.

*I6_2_5

* Define Multiple Response Sets.
MRSETS
  /MCGROUP NAME=$I6_2_5 VARIABLES=I6_2_5_71 I6_2_5_72 I6_2_5_73 I6_2_5_98 I6_2_5_99
  /DISPLAY NAME=[$I6_2_5].

*Custom TaBles.
CTABLES
/VLABELS VARIABLES=$I6_2_5 DISPLAY=none
/TABLE $I6_2_5   [COLPCT.COUNT PCT40.1, totals [count f40.0]]  
/slabels position=row
/slabels visible=no
/CATEGORIES VARIABLES= $I6_2_5 ORDER=A KEY=VALUE EMPTY=INCLUDE  total=yes position=before.
 

*I6_2_7 

* Define Multiple Response Sets.
MRSETS
  /MCGROUP NAME=$I6_2_7 VARIABLES=I6_2_7_71 I6_2_7_72 I6_2_7_73 I6_2_7_98 I6_2_7_99
  /DISPLAY NAME=[$I6_2_7].

*Custom TaBles.
CTABLES
/VLABELS VARIABLES=$I6_2_7  DISPLAY=none
/TABLE $I6_2_7   [COLPCT.COUNT PCT40.1, totals [count f40.0]]  
/slabels position=row
/slabels visible=no
/CATEGORIES VARIABLES= $I6_2_7   ORDER=A KEY=VALUE EMPTY=INCLUDE  total=yes position=before.


*I6_2_8 

* Define Multiple Response Sets.
MRSETS
  /MCGROUP NAME=$I6_2_8 VARIABLES=I6_2_8_71 I6_2_8_72 I6_2_8_73 I6_2_8_98 I6_2_8_99
  /DISPLAY NAME=[$I6_2_8].

*Custom TaBles.
CTABLES
/VLABELS VARIABLES=$I6_2_8  DISPLAY=none
/TABLE $I6_2_8   [COLPCT.COUNT PCT40.1, totals [count f40.0]]  
/slabels position=row
/slabels visible=no
/CATEGORIES VARIABLES= $I6_2_8   ORDER=A KEY=VALUE EMPTY=INCLUDE  total=yes position=before.

*I6_2 для соединений 5,7,8 в total

if I6_2_5_71=71 or I6_2_7_71=71 or I6_2_8_71=71 I6_2_578_71=71. 
if I6_2_5_72=72 or I6_2_7_72=72 or I6_2_8_72=72 I6_2_578_72=72. 
if I6_2_5_73=73 or I6_2_7_73=73 or I6_2_8_73=73 I6_2_578_73=73. 
if I6_2_5_98=98 or I6_2_7_98=98 or I6_2_8_98=98 I6_2_578_98=98. 
EXECUTE.

if QI20_5=1 and QI20_7=1 and QI20_8=1 and I6_2_5_99=99 and I6_2_7_99=99 and I6_2_8_99=99 I6_2_578_99=99. 
if QI20_5=1 and QI20_7>1 and QI20_8=1 and I6_2_5_99=99 and I6_2_8_99=99 I6_2_578_99=99. 
if QI20_5=1 and QI20_7=1 and QI20_8>1 and I6_2_5_99=99 and I6_2_7_99=99 I6_2_578_99=99. 
if QI20_5>1 and QI20_7=1 and QI20_8=1 and I6_2_7_99=99 and I6_2_8_99=99 I6_2_578_99=99. 
EXECUTE.

if QI20_5=1 and QI20_7>1 and QI20_8>1 and I6_2_5_99=99 I6_2_578_99=99. 
if QI20_5>1 and QI20_7>1 and QI20_8=1 and I6_2_8_99=99 I6_2_578_99=99. 
if QI20_5>1 and QI20_7=1 and QI20_8>1 and I6_2_7_99=99 I6_2_578_99=99. 
EXECUTE.

do IF QI1=1 and M2_99code=0 and QI20_5=1.
Recode I6_2_578_71 I6_2_578_72 I6_2_578_73 I6_2_578_98 I6_2_578_99   (sysmis=0).	
End if.
EXECUTE.

do IF QI1=1 and M2_99code=0 and QI20_7=1.
Recode I6_2_578_71 I6_2_578_72 I6_2_578_73 I6_2_578_98 I6_2_578_99   (sysmis=0).	
End if.
EXECUTE.

do IF QI1=1 and M2_99code=0 and QI20_8=1.
Recode I6_2_578_71 I6_2_578_72 I6_2_578_73 I6_2_578_98 I6_2_578_99   (sysmis=0).	
End if.
EXECUTE.


* Define Multiple Response Sets.
MRSETS
  /MCGROUP NAME=$I6_2_578 VARIABLES=I6_2_578_71 I6_2_578_72 I6_2_578_73 I6_2_578_98 I6_2_578_99
  /DISPLAY NAME=[$I6_2_578].

*Custom TaBles.
CTABLES
/VLABELS VARIABLES=$I6_2_578 DISPLAY=none
/TABLE $I6_2_578   [COLPCT.COUNT PCT40.1, totals [count f40.0]]  
/slabels position=row
/slabels visible=no
/CATEGORIES VARIABLES= $I6_2_578   ORDER=A KEY=VALUE EMPTY=INCLUDE  total=yes position=before .


