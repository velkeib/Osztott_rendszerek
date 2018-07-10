Az agent.AgentMain f�program parancssori param�terk�nt megkapja a titkos�gyn�k�k darabsz�m�t (n �s m), �s elind�t mindegyik�knek egy-egy sz�lat. A f�program megkapja tov�bb� a programban sz�ks�ges v�rakoz�s hossz�nak als� �s fels� korl�tj�t ezredm�sodpercben m�rve (t1 �s t2).

Mindegyik titkos�gyn�k adatai egy f�jlban vannak le�rva. A f�jl neve agentO-U.txt, ahol O �rt�ke 1 vagy 2 att�l f�gg�en, hogy melyik �gyn�ks�gbe tartozik az �gyn�k, �s U az �gyn�k sorsz�ma. P�ld�ul agent2-5.txt a m�sodik �gyn�ks�g �t�s sorsz�m� �gyn�k�hez tartozik. A f�jl k�t sort tartalmaz.

Az els� sorban az �gyn�k �lnevei szerepelnek sz�k�z�kkel elv�lasztva. Feltehet�, hogy a j�t�kban minden �ln�v k�l�nb�z�.
A m�sodik sor egy titkos �zenet egy szav�t tartalmazza.
Az �gyn�k�k indul�skor v�letlenszer�en v�lasztanak egy portot a 20000..20100 intervallumban. Ha a port foglalt, akkor �jat v�lasztanak eg�sz addig, am�g nem siker�lt saj�t portot tal�lniuk. Ezut�n mindegyik �gyn�k k�t tev�kenys�gbe kezd bele egym�st�l f�ggetlen�l.

Egyr�szt elkezd v�rni egy kapcsolatra legfeljebb t2 ideig (a hossz be�ll�t�s�hoz l�sd setSoTimeout).
Ha az id� lej�rt, vagy egy kapcsolat lezajlott, akkor bez�rja a portot, �s egy m�sik portot keres v�letlenszer�en.
M�sr�szt v�letlenszer�en v�lasztott (t1..t2 k�z�tti ezredm�sodperc) id� eltelt�vel megpr�b�l egy m�sik szerverhez csatlakozni.
A szerver portj�t szint�n a 20000..20100 intervallumb�l v�lasztja, persze a saj�t portja kiv�tel�vel.
Ak�r siker�lt kapcsolatot teremteni, ak�r nem, t1..t2 id� eltelt�vel �jra pr�b�lkozik.
Amikor kapcsolatba ker�l egym�ssal a k�t f�l, a k�vetkez�k t�rt�nnek.

A szerver elk�ldi az �lnevei k�z�l az egyiket v�letlenszer�en.
Erre a kliens elk�ldi azt, hogy szerinte a szerver melyik �gyn�ks�ghez tartozik.
Ha a kliens m�r tal�lkozott ezzel az �ln�vvel, akkor tudja a helyes v�laszt erre a k�rd�sre, �s azt k�ldi el, k�l�nben tippel.
Ha a kliens t�vedett, akkor a szerver bontja a kapcsolatot.
K�l�nben a szerver elk�ldi az OK sz�veget.
A kliens, ha azonos �gyn�ks�ghez tartozik, elk�ldi az OK sz�veget, majd mindketten elk�ldenek egy-egy titkos sz�veget a m�siknak, amit ismernek, �s ezut�n bontj�k a kapcsolatot. A kapott titkot mindketten felveszik az ismert titkaik k�z�.
Ha a titkot m�r ismerte, aki megkapja, az nem baj. Az is el�fordulhat, hogy ismerte, �s m�r el is �rulta valakinek.
Ez a k�ld�s nem sz�m�t a titok el�rul�s�nak.
A kliens, ha a m�sik �gyn�ks�ghez tartozik, elk�ldi a ??? sz�veget, majd egy sz�mot, ami szerinte a m�sik �gyn�k sorsz�ma lehet. (Ha m�r tal�lkozott vele, akkor olyan tippet nem ad, ami biztosan t�ves.)
A szerver azonnal bontja a kapcsolatot, ha t�ves a sorsz�m.
Ha helyes a sorsz�m, elk�ldi az �ltala ismert titkok egyik�t. Ha t�bbet is ismer, v�letlenszer�en v�lasztja ki, melyiket �rulja el, de csak olyat, amelyiket eddig m�g nem �rult el.
Az el�rult titok megmarad az �gyn�kn�l, de meglesz a m�sik �gyn�kn�l is.
Ezut�n, ha m�r minden �ltala ismert titkot el�rult, a szerveroldali �gyn�k le van tart�ztatva, �s nem v�gez t�bb tev�kenys�get (bez�rja a portj�t; nem fogad �s nem kezdem�nyez t�bb kapcsolatot).
Ha van m�g legal�bb egy olyan titok, amelyet nem �rult el, folytatja a tev�kenys�g�t.
A j�t�knak az al�bbi esetekben van v�ge.

Az egyik �gyn�ks�g �sszes tagj�t letart�ztatt�k. A m�sik �gyn�ks�g gy�z�tt.
Az egyik �gyn�k megszerezte a m�sik csapat �sszes titk�t. Az � �gyn�ks�ge gy�z�tt.