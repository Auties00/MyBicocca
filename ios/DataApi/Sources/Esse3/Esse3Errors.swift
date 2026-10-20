import Foundation

/// The roles Esse3 operations may require, as declared by their specs.
public enum Esse3PermissionLevel: String, Hashable, Sendable, CaseIterable {
    case any = "ANY"
    case authenticatedUser = "AUTHENTICATED_USER"
    case teacher = "TEACHER"
    case provisionalEnrolledStudent = "PROVISIONAL_ENROLLED_STUDENT"
    case registeredUser = "REGISTERED_USER"
    case adminOffice = "ADMIN_OFFICE"
    case adminOfficeAdmin = "ADMIN_OFFICE_ADMIN"
    case externalSubject = "EXTERNAL_SUBJECT"
    case student = "STUDENT"
    case technicalUser = "TECHNICAL_USER"
    case unknown = "UNKNOWN"
}

/// The error body Esse3 sends with a failed request.
public struct Esse3ErrorResponse: Decodable, Hashable, Sendable {
    public var statusCode: Int?
    public var returnCode: Int?
    public var errorMessage: String
    public var errorDetails: [Esse3ErrorDetail]

    public init(statusCode: Int? = nil, returnCode: Int? = nil, errorMessage: String = "", errorDetails: [Esse3ErrorDetail] = []) {
        self.statusCode = statusCode
        self.returnCode = returnCode
        self.errorMessage = errorMessage
        self.errorDetails = errorDetails
    }

    private enum CodingKeys: String, CodingKey {
        case statusCode
        case returnCode = "retCode"
        case errorMessage = "retErrMsg"
        case errorDetails = "errDetails"
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        statusCode = try container.decodeIfPresent(Int.self, forKey: .statusCode)
        returnCode = try container.decodeIfPresent(Int.self, forKey: .returnCode)
        errorMessage = try container.decodeIfPresent(String.self, forKey: .errorMessage) ?? ""
        errorDetails = try container.decodeIfPresent([Esse3ErrorDetail].self, forKey: .errorDetails) ?? []
    }
}

public struct Esse3ErrorDetail: Decodable, Hashable, Sendable {
    public var errorType: String
    public var value: String?
    public var rawValue: String?
}

/// A request Esse3 rejected.
public struct Esse3Error: Error, CustomStringConvertible {
    public let status: Int
    /// The roles the operation requires, a hint when the status is 401 or 403.
    public let expectedRoles: Set<Esse3PermissionLevel>
    public let response: Esse3ErrorResponse

    public var description: String {
        var lines = ["Esse3 request failed with status \(status)"]
        if let returnCode = response.returnCode { lines.append("Return code: \(returnCode)") }
        if !expectedRoles.isEmpty { lines.append("Expected roles: \(expectedRoles.map(\.rawValue).sorted().joined(separator: ", "))") }
        if !response.errorMessage.isEmpty { lines.append("Message: \(response.errorMessage)") }
        for detail in response.errorDetails {
            lines.append("- \(detail.errorType)" + ((detail.value ?? detail.rawValue).map { ": \($0)" } ?? ""))
        }
        return lines.joined(separator: "\n")
    }
}
