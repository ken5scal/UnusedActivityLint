# Getting Started

============

##### Fetch code
```
git clone https://github.com/ken5scal/UnusedActivityLint.git
cd android-custom-lint-rules
```

##### Build the validator

`./gradlew build`

##### Copy to the lint directory

`cp ./whatever_your_workind_dir/android-custom-lint-rules.jar ~/.android/lint/`

##### Verify whether the issues are registered with lint

`lint --show UnusedActivityLint`

##### Run lint

`./gradlew lint`

> Note: If you can't run `lint` directly, you may want to include android tools `PATH` in your
 `~/.bash_profile`.
> (i.e. `PATH=$PATH:~/Library/Android/sdk/tools`)
>
> Then run `source ~/.bash_profile`.

Support
-------
If you've found an error, suggestion, recommendation, please file an issue:
https://github.com/ken5scal/UnusedActivityLint/issues

