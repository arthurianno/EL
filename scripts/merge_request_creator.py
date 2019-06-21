import json
from argparse import ArgumentParser
from subprocess import check_output

import requests


def get_git_branches():
    out = check_output(["git", "branch"]).decode("utf8")
    branches = out.split("\n")
    return branches


def find_current_branch(branches):
    current = next(branch for branch in branches if branch.startswith("*"))
    output = current.strip("*").strip()
    # print("current branch: " + output)
    return output


def find_parent_branch(branches, current):
    tokens = current.split("/")
    type = tokens[0]
    parent = 'develop'
    if type == 'subtask':
        parent_prefix = tokens[1]
        parent_prefix_lower = parent_prefix.lower()
        parent = next(branch for branch in branches if (parent_prefix_lower in branch.lower())).strip()

    # print("parent branch: " + parent)
    return parent


def get_project_path():
    out = check_output(["git", "remote", "get-url", "origin"]).decode("utf8")
    # git@192.168.1.1:project/key.git
    key = out.split(':')[1].replace('.git', '').strip()
    return key


def encode(string):
    encoded = string.replace('/', '%2F')
    return encoded


def create_merge_request(token, project_id, title, current, destination):
    headers = {
        'Private-Token': token,
        'content-type': 'application/json'
    }
    url = "http://172.16.100.20/api/v4/projects/" + project_id + "/merge_requests"
    data = {
        "id": project_id,
        "title": title,
        "target_branch": destination,
        "source_branch": current,
        "remove_source_branch": "true"
    }
    response = requests.post(url=url, headers=headers, data=json.dumps(data))

    mr = response.json()

    if response.status_code == 201:
        print("created: " + mr['web_url'])
    else:
        print("error: " + mr['message'][0])

    return mr


def get_title_from_branch(branch):
    title = branch.replace('-', ' ').lower()
    formatted = title[0].capitalize() + title[1:]
    return formatted


if __name__ == '__main__':
    parser = ArgumentParser()
    parser.add_argument("-t", "--token", dest="TOKEN")

    args = parser.parse_args()

    token = args.TOKEN
    project_path = get_project_path()

    all_branches = get_git_branches()
    current_branch = find_current_branch(all_branches)
    parent_branch = find_parent_branch(all_branches, current_branch)
    title = get_title_from_branch(current_branch)

    print("request: " + current_branch + " -> " + parent_branch)

    create_merge_request(
        token=token,
        project_id=encode(project_path),
        title=title,
        current=current_branch,
        destination=parent_branch
    )
