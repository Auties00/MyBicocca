@testable import DataApiRuntime
import XCTest

final class MultipartTests: XCTestCase {
    func testSendsFieldsAndFilesAsMultipartParts() throws {
        let configuration = RestConfiguration(baseURL: URL(string: "https://example.com")!)
        let file = FileUpload(fileName: "notes.txt", contentType: "text/plain", bytes: Data("hello".utf8))
        let request = try RestRequest(method: .post, path: "/upload", form: [("itemid", 0)], files: [("file", file)], multipart: true)
        let urlRequest = try configuration.urlRequest(for: request)
        let contentType = try XCTUnwrap(urlRequest.value(forHTTPHeaderField: "Content-Type"))
        XCTAssertTrue(contentType.hasPrefix("multipart/form-data; boundary="))
        let body = String(decoding: try XCTUnwrap(urlRequest.httpBody), as: UTF8.self)
        XCTAssertTrue(body.contains("name=\"itemid\"\r\n\r\n0\r\n"))
        XCTAssertTrue(body.contains("name=\"file\"; filename=\"notes.txt\"\r\nContent-Type: text/plain\r\n\r\nhello\r\n"))
    }
}
