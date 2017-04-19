package com.wolfie.odile.model.loader;

public class LoaderResult {

    public String mSuccessMessage;
    public String mFailureMessage;

    public static LoaderResult makeSuccess(String successMessage) {
        LoaderResult loaderResult = new LoaderResult();
        loaderResult.mSuccessMessage = successMessage;
        return loaderResult;
    }

    public static LoaderResult makeFailure(String failureMessage) {
        LoaderResult loaderResult = new LoaderResult();
        loaderResult.mFailureMessage = failureMessage;
        return loaderResult;
    }

}
