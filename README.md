# Komputerowe wspomaganie technik kryminalistycznych - projekt zespołowy (sem. letni 2015/2016) #

Projekt zespołowy składa się z 3 części, tj. z:

1. napisania aplikacji,
1. napisania dokumentacji ww. aplikacji,
1. prezentacji.

### Temat projektu ###

> Identyfikacja twarzy.

### Słowa kluczowe ###

- [Eigenface](https://www.google.pl/search?q=Eigenface),
- [Principal component analysis (PCA) (analiza głównych składowych)](https://en.wikipedia.org/wiki/Principal_component_analysis),
- [Eigenvalues and eigenvectors (wartości i wektory własne)](https://en.wikipedia.org/wiki/Eigenvalues_and_eigenvectors),
- [Face identification (**nie** *recognition*)](https://www.google.pl/search?q=face+identification).

### Deadline'y ###

Data | Co?
---|---
4 III 2016 | wybór tematów projektów
1 IV 2016 | omówienie postępów prac projektowych
15 IV 2016 | kolokwium (1 godz. zegarowa)
29 IV 2016 | omówienie postępów prac projektowych i ustalenie wymagań końcowych
3 VI 2016 | przekazanie wykładowcy wersji beta projektów do wstępnej oceny
15 VI 2016 (środa) | kolokwium (1 godz. zegarowa) + prezentacja (publiczna obrona) projektów

[Terminarz](https://staff.elka.pw.edu.pl/~mszezyns/CAF/index.html) i [strona domowa](https://staff.elka.pw.edu.pl/~mszezyns/CAF/index.html) Wykładowcy.

### Linki ###
- [Strona przedmiotu](https://staff.elka.pw.edu.pl/~mszezyns/CAF/index.html),
- [The Eigenfaces method - Face Recognition and Biometric Systems (Politechnika Śląska)](http://sun.aei.polsl.pl/~mkawulok/stud/fr/lect/07.pdf),
- [Principal component analysis (PCA) - Wikipedia](https://en.wikipedia.org/wiki/Principal_component_analysis),
- [Eigenface - Wikipedia](https://en.wikipedia.org/wiki/Eigenface),
- [Eigenfaces (YouTube)](https://www.youtube.com/watch?v=_lY74pXWlS8),
- [Face Recognition (FBI)](https://www.fbi.gov/about-us/cjis/fingerprints_biometrics/biometric-center-of-excellence/files/face-recognition.pdf),
- [Face Recognition in OpenCV](http://docs.opencv.org/2.4/modules/contrib/doc/facerec/facerec_api.html).

### Użyte technologie i oprogramowanie ###

Prawdopodobnie:
- [Java](https://www.java.com/),
- [OpenCV](http://opencv.org/),
- [Eclipse](https://eclipse.org/),
- [Git](https://git-scm.com/),
- [LaTeX](https://www.latex-project.org/) (do dokumentacji, [AFAIR](https://en.wiktionary.org/wiki/AFAIR) obowiązkowy).

Uzasadnienie: [OpenCV](http://opencv.org/), który ma prawdopodobnie zaimplementowaną identyfikację twarzy przy użyciu *eigenfaces*, jest wspierany w *C++* i *Javie*. Nie istnieją oficjalne porty na *C#*, a istniejące [działają gorzej](http://stackoverflow.com/questions/85569/net-dotnet-wrappers-for-opencv) od oficjalnego [OpenCV](http://opencv.org/).
