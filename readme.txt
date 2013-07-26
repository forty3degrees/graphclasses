SOFTWARE PROJEKT SS2013
=======================

Team: CENT (Calum McLellan, Tino Klingebiel, Niklas Spitzer)

Dokumente (graphclasses.zip): 

Quell-Code:		'graphclasses\K\p\*'
Dokumentation:		'graphclasses\documentation\*'
		
			 Davon noch nicht abgebebene Dokumente:

				'UI Documentation.pdf'
				'external\Protokoll*' (pdfs)
				'internal\System Design Document.pdf' (Revision 2)
				'tests\Dokumentation der Projekttests-CENT.pdf'

Installationsanleitung: 'Anleitung zur Installation des Programms.pdf' (auf der oberste Ebene von graphclasses.zip)

Notizen:

Es wird die erste Phase der notwendige Refactoring gemacht. Dadurch wird der Code in
der 3 Ebenen aus dem SDD aufgeteilt. Die Trennung der Ebenen ist soweit abgeschlossen, 
jedoch innerhalb der existierenden Code sind noch abh�ngigkeiten die mit eine 2. 
Refactoring Phase eliminiert werden sollten. Diese Phase war aber in der verf�gbare
Zeit nicht m�glich, und war auch von uns nie vorgesehen (es ist ja keine kleine 
Aufgabe).
Die Klassen im folgenden Packages sind vollst�ndig �berarbeitet und reflektieren dass
was wir f�r 'sauberen' Code halten (ausreichend kommentiert, gut strukturiert und keine
Warnungen):
	teo.isgci.core	
	teo.isgci.data	
	teo.isgci.yfiles	

Es gibt zwei Teile vom Code die mit // TODO SWP: versehen sind. Eine davon ist in der
Methode YFilesDrawingManager.search(NodeView), der andere in den Konstruktor vom
ISGCIMainFrame. Bei 'search' handelt es sich um einen, unsere Meinung nach, sinvolle
Erweiterung/Sch�nheitsfunktion. Bei den anderen geht es um einen auskommentierten Teil
der alten Code wof�r wir nicht feststellen k�nnen, ob dies noch notwendig ist - f�r das
neue Programm ist es auf jeden Fall nicht notwendig, aber wenn man unseren Code weiter
verwenden will dann k�nnte es evtl. von Bedeutung sein.

