package com.example.csci310_teamproj.data.repository;

public interface RepositoryCallback<T> {
    void onSuccess(T result);
    void onError(String error);
}
