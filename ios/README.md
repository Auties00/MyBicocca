# MyBicocca for iOS

The iOS app. It consumes the Swift SDKs generated from the specs in [`../common`](../common).

## Data APIs

[`DataApi`](DataApi) is a Swift package with one library per data API:

- `DataApiRuntime` — the hand-written runtime every SDK uses (dates, lenient decoding, requests)
- `Affluences` — generated sources in `Generated/` plus the hand-written `AffluencesClient`
- `Esse3` — generated sources in `Generated/` plus the hand-written `Esse3Client`

```swift
let esse3 = Esse3Api(configuration: RestConfiguration(
    baseURL: Esse3Client.defaultBaseURL,
    headers: ["Authorization": "Basic …"]
))
let careers = try await esse3.careers.getCareers(userId: "…")
```

Regenerate the SDKs with the generator in [`../common/codegen`](../common/codegen).
