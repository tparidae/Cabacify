package arithmeticcoding;

public class AlphabetParameterBinarizer {

    private int _previousAlphabetIdx;
    private final Binarisation[] _binarisations;
    private final int _maxAlphabetIdx;

    public AlphabetParameterBinarizer(int numAlphabets) {
        this._maxAlphabetIdx = numAlphabets - 1;
        this._previousAlphabetIdx = 0;

        this._binarisations = new Binarisation[numAlphabets];
        for (int i = 0; i < numAlphabets; i++) {
            _binarisations[i] = Binarisation.binariseTruncatedUnary(i, _maxAlphabetIdx);
        }
    }

    public Binarisation binariseAlphabetParameter(int alphabetIdx) {
        if (_previousAlphabetIdx == alphabetIdx) {
            return _binarisations[0];
        } else {
            if (_previousAlphabetIdx == 0) {
                if (alphabetIdx == 1) {
                    return _binarisations[1];
                }
                if (alphabetIdx == 2) {
                    return _binarisations[2];
                }
                if (alphabetIdx == 3) {
                    return _binarisations[3];
                }
            } else if (_previousAlphabetIdx == 1) {
                if (alphabetIdx == 0) {
                    return _binarisations[1];
                }
                if (alphabetIdx == 2) {
                    return _binarisations[2];
                }
                if (alphabetIdx == 3) {
                    return _binarisations[3];
                }
            } else if (_previousAlphabetIdx == 2) {
                if (alphabetIdx == 0) {
                    return _binarisations[1];
                }
                if (alphabetIdx == 1) {
                    return _binarisations[2];
                }
                if (alphabetIdx == 3) {
                    return _binarisations[3];
                }
            }else if (_previousAlphabetIdx == 3) {
                if (alphabetIdx == 0) {
                    return _binarisations[1];
                }
                if (alphabetIdx == 1) {
                    return _binarisations[2];
                }
                if (alphabetIdx == 2) {
                    return _binarisations[3];
                }
            }
        }
        return null;
    }

    public boolean isAlphabetBinarisation(Binarisation bin) {
        return Binarisation.isTruncatedUnary(bin, _maxAlphabetIdx);
    }

    public int unBinariseAlphabetParameter(Binarisation bin) {
        int bins = Binarisation.unBinariseTruncatedUnary(bin, _maxAlphabetIdx);

        if (bin.equals(_binarisations[0])) {
            return _previousAlphabetIdx;
        } else {
            if (_previousAlphabetIdx == 0) {
                if (bin.equals(_binarisations[1])) {
                    return 1;
                } else if (bin.equals(_binarisations[2])) {
                    return 2;
                }else if (bin.equals(_binarisations[3])) {
                    return 3;
                }
            } else if (_previousAlphabetIdx == 1) {
                if (bin.equals(_binarisations[1])) {
                    return 0;
                } else if (bin.equals(_binarisations[2])) {
                    return 2;
                }else if (bin.equals(_binarisations[3])) {
                    return 3;
                }
            } else if (_previousAlphabetIdx == 2) {
                if (bin.equals(_binarisations[1])) {
                    return 0;
                } else if (bin.equals(_binarisations[2])) {
                    return 1;
                }else if (bin.equals(_binarisations[3])) {
                    return 3;
                }
            }else if (_previousAlphabetIdx == 3) {
                if (bin.equals(_binarisations[1])) {
                    return 0;
                } else if (bin.equals(_binarisations[2])) {
                    return 1;
                }else if (bin.equals(_binarisations[3])) {
                    return 2;
                }
            }
        }

        return 0;
    }

    public void update(int alphabetIdx) {
        _previousAlphabetIdx = alphabetIdx;
    }

    public void reset() {
        _previousAlphabetIdx = 0;
    }

}
